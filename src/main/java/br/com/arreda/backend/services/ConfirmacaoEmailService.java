package br.com.arreda.backend.services;

import br.com.arreda.backend.enums.TipoToken;
import br.com.arreda.backend.models.Usuario;
import br.com.arreda.backend.repositories.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Map;

/** Fluxo de confirmação de e-mail: envio do link (cadastro e reenvio) e confirmação da conta. */
@Service
public class ConfirmacaoEmailService {

    private static final Logger log = LoggerFactory.getLogger(ConfirmacaoEmailService.class);
    private static final String ASSUNTO_EMAIL = "Confirme seu e-mail | Arreda";
    private static final String TEMPLATE = "/templates/email-confirmacao.html";

    /** 1 reenvio por e-mail a cada 2 minutos (o frontend mostra o mesmo contador). */
    private final LimiteSolicitacoes limiteReenvio = new LimiteSolicitacoes(1, Duration.ofMinutes(2));

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TokenVerificacaoService tokenVerificacaoService;

    @Autowired
    private EmailService emailService;

    @Value("${arreda.frontend.url}")
    private String frontendUrl;

    /** Gera o token CONFIRMACAO_EMAIL (24h) e dispara o e-mail. Falhas de e-mail só são registradas no log. */
    public void enviarConfirmacao(Usuario usuario) {
        try {
            String token = tokenVerificacaoService.gerar(usuario, TipoToken.CONFIRMACAO_EMAIL).getToken();
            String corpo = TemplateEmail.renderizar(TEMPLATE, Map.of(
                    "nome", usuario.getNome(),
                    "linkConfirmacao", TemplateEmail.link(frontendUrl, "confirmar-email?token=" + token)));
            emailService.enviar(usuario.getEmail(), ASSUNTO_EMAIL, corpo);
        } catch (Exception excecao) {
            // O cadastro não deve falhar por problema de e-mail; o usuário pode pedir o reenvio.
            log.error("Falha ao preparar envio do e-mail de confirmação.", excecao);
        }
    }

    /**
     * Registra o pedido de reenvio se estiver dentro do limite por e-mail.
     * O limite vale para qualquer e-mail informado, exista ou não a conta, para não revelar cadastros.
     */
    public boolean podeReenviar(String email) {
        boolean permitido = limiteReenvio.tentarRegistrar(email);
        if (!permitido) {
            log.info("Limite de reenvio de confirmação atingido para o e-mail informado.");
        }
        return permitido;
    }

    /** Reenvia o link apenas para contas existentes e ainda não verificadas; nos demais casos não faz nada. */
    public void reenviar(String email) {
        usuarioRepository.findByEmail(email.trim().toLowerCase())
                .filter(usuario -> !usuario.isEmailVerificado())
                .ifPresent(this::enviarConfirmacao);
    }

    /**
     * Confirma o e-mail do dono do token. Token inexistente, adulterado, expirado ou já usado
     * gera RegraDeNegocioException (400 com mensagem amigável via GlobalExceptionHandler).
     */
    @Transactional
    public void confirmar(String token) {
        Usuario usuario = tokenVerificacaoService.validarEConsumir(token, TipoToken.CONFIRMACAO_EMAIL);
        usuario.setEmailVerificado(true);
        usuarioRepository.save(usuario);
    }
}
