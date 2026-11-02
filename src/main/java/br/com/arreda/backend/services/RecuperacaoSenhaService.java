package br.com.arreda.backend.services;

import br.com.arreda.backend.dto.RedefinirSenhaDTO;
import br.com.arreda.backend.enums.TipoToken;
import br.com.arreda.backend.models.Usuario;
import br.com.arreda.backend.repositories.RefreshTokenRepository;
import br.com.arreda.backend.repositories.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Map;

/** Fluxo "Esqueci minha senha": geração do token RESET_SENHA + e-mail, e redefinição da senha. */
@Service
public class RecuperacaoSenhaService {

    private static final Logger log = LoggerFactory.getLogger(RecuperacaoSenhaService.class);
    private static final String ASSUNTO_EMAIL = "Redefinição de senha | Arreda";
    private static final String TEMPLATE = "/templates/email-redefinicao-senha.html";

    /** 3 solicitações por e-mail a cada 15 minutos. */
    private final LimiteSolicitacoes limiteSolicitacoes = new LimiteSolicitacoes(3, Duration.ofMinutes(15));

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TokenVerificacaoService tokenVerificacaoService;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${arreda.frontend.url}")
    private String frontendUrl;

    /**
     * Registra a solicitação se estiver dentro do limite por e-mail.
     * A decisão de resposta (200 ou 429) é idêntica exista ou não a conta,
     * para não revelar quem está cadastrado.
     *
     * @return true quando a solicitação pode seguir (limite não atingido).
     */
    public boolean podeSolicitar(String email) {
        boolean permitido = limiteSolicitacoes.tentarRegistrar(email);
        if (!permitido) {
            log.info("Limite de solicitações de redefinição atingido para o e-mail informado.");
        }
        return permitido;
    }

    /** Gera o token RESET_SENHA (30 min) e dispara o e-mail. Falhas de e-mail só são registradas no log. */
    public void solicitar(String email) {
        usuarioRepository.findByEmail(email.trim().toLowerCase()).ifPresent(usuario -> {
            try {
                String token = tokenVerificacaoService.gerar(usuario, TipoToken.RESET_SENHA).getToken();
                String corpo = TemplateEmail.renderizar(TEMPLATE, Map.of(
                        "nome", usuario.getNome(),
                        "linkRedefinicao", TemplateEmail.link(frontendUrl, "redefinir-senha?token=" + token)));
                emailService.enviar(usuario.getEmail(), ASSUNTO_EMAIL, corpo);
            } catch (Exception excecao) {
                // Nunca derruba a requisição por problema de e-mail; o comportamento segue idêntico.
                log.error("Falha ao preparar envio do e-mail de redefinição de senha.", excecao);
            }
        });
    }

    /**
     * Redefine a senha do dono do token: grava a nova com BCrypt, consome o token
     * (válido, expirado ou reutilizado já são tratados pelo TokenVerificacaoService, que
     * também invalida os demais links de redefinição pendentes) e revoga todos os RefreshToken do usuário.
     */
    @Transactional
    public void redefinir(RedefinirSenhaDTO dto) {
        Usuario usuario = tokenVerificacaoService.validarEConsumir(dto.token(), TipoToken.RESET_SENHA);
        usuario.setSenhaHash(passwordEncoder.encode(dto.novaSenha()));
        usuarioRepository.save(usuario);
        refreshTokenRepository.deleteByUsuarioId(usuario.getId());
    }
}
