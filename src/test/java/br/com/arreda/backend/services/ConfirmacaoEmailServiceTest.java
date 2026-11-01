package br.com.arreda.backend.services;

import br.com.arreda.backend.enums.TipoToken;
import br.com.arreda.backend.exception.RegraDeNegocioException;
import br.com.arreda.backend.models.TokenVerificacao;
import br.com.arreda.backend.models.Usuario;
import br.com.arreda.backend.repositories.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfirmacaoEmailServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private TokenVerificacaoService tokenVerificacaoService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private ConfirmacaoEmailService service;

    private Usuario usuario;

    @BeforeEach
    void configurar() {
        ReflectionTestUtils.setField(service, "frontendUrl", "http://localhost:5173");
        usuario = new Usuario();
        usuario.setId(3L);
        usuario.setNome("João Lima");
        usuario.setEmail("joao@exemplo.com");
    }

    private void tokenGerado(String valor) {
        TokenVerificacao token = new TokenVerificacao();
        token.setToken(valor);
        when(tokenVerificacaoService.gerar(usuario, TipoToken.CONFIRMACAO_EMAIL)).thenReturn(token);
    }

    @Test
    void enviarConfirmacao_geraTokenEEnviaEmailComLink() {
        tokenGerado("tok-1");

        service.enviarConfirmacao(usuario);

        verify(emailService).enviar(
                eq("joao@exemplo.com"),
                contains("Confirme"),
                argThat(html -> html.contains("http://localhost:5173/confirmar-email?token=tok-1")
                        && html.contains("João Lima")
                        && !html.contains("{{")));
    }

    @Test
    void enviarConfirmacao_comFalhaNoEnvio_naoPropagaExcecao() {
        tokenGerado("tok-1");
        doThrow(new MailSendException("SMTP fora do ar")).when(emailService)
                .enviar(anyString(), anyString(), anyString());

        assertDoesNotThrow(() -> service.enviarConfirmacao(usuario));
    }

    @Test
    void reenviar_contaNaoVerificada_enviaNovoLink() {
        when(usuarioRepository.findByEmail("joao@exemplo.com")).thenReturn(Optional.of(usuario));
        tokenGerado("tok-2");

        service.reenviar("  Joao@Exemplo.com ");

        verify(emailService).enviar(eq("joao@exemplo.com"), anyString(), contains("token=tok-2"));
    }

    @Test
    void reenviar_contaJaVerificada_naoEnviaNada() {
        usuario.setEmailVerificado(true);
        when(usuarioRepository.findByEmail("joao@exemplo.com")).thenReturn(Optional.of(usuario));

        service.reenviar("joao@exemplo.com");

        verify(tokenVerificacaoService, never()).gerar(any(), any());
        verify(emailService, never()).enviar(anyString(), anyString(), anyString());
    }

    @Test
    void reenviar_emailDesconhecido_naoEnviaNada() {
        when(usuarioRepository.findByEmail("fantasma@exemplo.com")).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> service.reenviar("fantasma@exemplo.com"));

        verify(emailService, never()).enviar(anyString(), anyString(), anyString());
    }

    @Test
    void podeReenviar_permiteUmEBloqueiaOSegundo() {
        assertTrue(service.podeReenviar("joao@exemplo.com"));
        assertFalse(service.podeReenviar("JOAO@exemplo.com"));
    }

    @Test
    void confirmar_marcaEmailComoVerificado() {
        when(tokenVerificacaoService.validarEConsumir("tok-1", TipoToken.CONFIRMACAO_EMAIL)).thenReturn(usuario);

        service.confirmar("tok-1");

        assertTrue(usuario.isEmailVerificado());
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void confirmar_tokenInvalido_naoAlteraConta() {
        when(tokenVerificacaoService.validarEConsumir(anyString(), eq(TipoToken.CONFIRMACAO_EMAIL)))
                .thenThrow(new RegraDeNegocioException("Link invalido"));

        assertThrows(RegraDeNegocioException.class, () -> service.confirmar("adulterado"));

        verify(usuarioRepository, never()).save(any());
    }
}
