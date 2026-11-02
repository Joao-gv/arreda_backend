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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Fluxo "Esqueci minha senha": geração do token RESET_SENHA + e-mail, e redefinição da senha. */
@Service
public class RecuperacaoSenhaService {

    private static final Logger log = LoggerFactory.getLogger(RecuperacaoSenhaService.class);
    private static final int LIMITE_SOLICITACOES = 3;
    private static final int JANELA_MINUTOS = 15;
    private static final String ASSUNTO_EMAIL = "Redefinição de senha | Arreda";
    private static final String TEMPLATE = "/templates/email-redefinicao-senha.html";

    // ponytail: rate limit em memória (por e-mail, 3 a cada 15 min).
    // Zera no restart e vale apenas para 1 instância; migrar para tabela/Redis
    // quando o backend rodar em mais de uma instância ou precisar sobreviver a restarts.
    private final Map<String, List<LocalDateTime>> solicitacoesPorEmail = new HashMap<>();

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
        List<LocalDateTime> solicitacoes = solicitacoesPorEmail
                .computeIfAbsent(email.trim().toLowerCase(), chave -> new ArrayList<>());
        LocalDateTime limiteDaJanela = LocalDateTime.now().minusMinutes(JANELA_MINUTOS);
        solicitacoes.removeIf(data -> data.isBefore(limiteDaJanela));
        if (solicitacoes.size() >= LIMITE_SOLICITACOES) {
            log.info("Limite de solicitações de redefinição atingido para o e-mail informado.");
            return false;
        }
        solicitacoes.add(LocalDateTime.now());
        return true;
    }

    /** Gera o token RESET_SENHA (30 min) e dispara o e-mail. Falhas de e-mail só são registradas no log. */
    public void solicitar(String email) {
        usuarioRepository.findByEmail(email.trim()).ifPresent(usuario -> {
            try {
                String token = tokenVerificacaoService.gerar(usuario, TipoToken.RESET_SENHA).getToken();
                String corpo = montarCorpoEmail(usuario.getNome(), token);
                emailService.enviar(email.trim(), ASSUNTO_EMAIL, corpo);
            } catch (Exception excecao) {
                // Nunca derruba a requisição por problema de e-mail; o comportamento segue idêntico.
                log.error("Falha ao preparar envio do e-mail de redefinição de senha.", excecao);
            }
        });
    }

    /**
     * Redefine a senha do dono do token: grava a nova com BCrypt, consome o token
     * (válido, expirado ou reutilizado já são tratados pelo TokenVerificacaoService)
     * e revoga todos os RefreshToken do usuário.
     */
    @Transactional
    public void redefinir(RedefinirSenhaDTO dto) {
        Usuario usuario = tokenVerificacaoService.validarEConsumir(dto.token(), TipoToken.RESET_SENHA);
        usuario.setSenhaHash(passwordEncoder.encode(dto.novaSenha()));
        usuarioRepository.save(usuario);
        refreshTokenRepository.deleteByUsuarioId(usuario.getId());
    }

    private String montarCorpoEmail(String nome, String token) {
        String link = frontendUrl.endsWith("/") ? frontendUrl : frontendUrl + "/";
        try (InputStream entrada = getClass().getResourceAsStream(TEMPLATE)) {
            if (entrada == null) {
                throw new IOException("Template não encontrado: " + TEMPLATE);
            }
            String html = new String(entrada.readAllBytes(), StandardCharsets.UTF_8);
            return html
                    .replace("{{nome}}", nome)
                    .replace("{{linkRedefinicao}}", link + "redefinir-senha?token=" + token);
        } catch (IOException excecao) {
            throw new IllegalStateException("Falha ao montar o e-mail de redefinição de senha.", excecao);
        }
    }
}
