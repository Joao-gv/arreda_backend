package br.com.arreda.backend.services;

import br.com.arreda.backend.dto.RedefinirSenhaDTO;
import br.com.arreda.backend.enums.TipoToken;
import br.com.arreda.backend.exception.RegraDeNegocioException;
import br.com.arreda.backend.models.TokenVerificacao;
import br.com.arreda.backend.models.Usuario;
import br.com.arreda.backend.repositories.RefreshTokenRepository;
import br.com.arreda.backend.repositories.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecuperacaoSenhaServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private TokenVerificacaoService tokenVerificacaoService;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RecuperacaoSenhaService service;

    private Usuario usuario;

    @BeforeEach
    void configurarFrontend() {
        ReflectionTestUtils.setField(service, "frontendUrl", "http://localhost:5173");
        usuario = new Usuario();
        usuario.setId(7L);
        usuario.setNome("Maria Souza");
        usuario.setEmail("maria@exemplo.com");
        usuario.setSenhaHash("hash-antigo");
        usuario.setTelefone("31900000007");
    }

    @Test
    void solicitar_comEmailCadastrado_geraTokenEEnviaEmailComLink() {
        when(usuarioRepository.findByEmail("maria@exemplo.com")).thenReturn(Optional.of(usuario));
        TokenVerificacao token = new TokenVerificacao();
        token.setToken("tok-123");
        when(tokenVerificacaoService.gerar(usuario, TipoToken.RESET_SENHA)).thenReturn(token);

        service.solicitar("  maria@exemplo.com  ");

        verify(tokenVerificacaoService).gerar(usuario, TipoToken.RESET_SENHA);
        verify(emailService).enviar(
                eq("maria@exemplo.com"),
                contains("Redefini"),
                argThat(html -> html.contains("redefinir-senha?token=tok-123")
                        && html.contains("Maria Souza")
                        && !html.contains("{{nome}}")
                        && !html.contains("{{linkRedefinicao}}")));
    }

    @Test
    void solicitar_comEmailDesconhecido_naoGeraTokenNemEnviaEmail() {
        when(usuarioRepository.findByEmail("fantasma@exemplo.com")).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> service.solicitar("fantasma@exemplo.com"));

        verify(tokenVerificacaoService, never()).gerar(any(), any());
        verify(emailService, never()).enviar(anyString(), anyString(), anyString());
    }

    @Test
    void solicitar_comFalhaNoEnvio_naoPropagaExcecao() {
        when(usuarioRepository.findByEmail("maria@exemplo.com")).thenReturn(Optional.of(usuario));
        TokenVerificacao token = new TokenVerificacao();
        token.setToken("tok-123");
        when(tokenVerificacaoService.gerar(usuario, TipoToken.RESET_SENHA)).thenReturn(token);
        doThrow(new MailSendException("SMTP fora do ar")).when(emailService)
                .enviar(anyString(), anyString(), anyString());

        assertDoesNotThrow(() -> service.solicitar("maria@exemplo.com"));

        verify(tokenVerificacaoService).gerar(usuario, TipoToken.RESET_SENHA);
    }

    @Test
    void redefinir_deveGravarNovoHashERevogarSessoes() {
        when(tokenVerificacaoService.validarEConsumir("tok-123", TipoToken.RESET_SENHA))
                .thenReturn(usuario);
        when(passwordEncoder.encode("Abcd1234x")).thenReturn("hash-novo");

        service.redefinir(new RedefinirSenhaDTO("tok-123", "Abcd1234x"));

        assertEquals("hash-novo", usuario.getSenhaHash());
        verify(usuarioRepository).save(usuario);
        verify(refreshTokenRepository).deleteByUsuarioId(7L);
    }

    @Test
    void redefinir_comTokenInvalido_lancaErroENaoAlteraNada() {
        when(tokenVerificacaoService.validarEConsumir(anyString(), eq(TipoToken.RESET_SENHA)))
                .thenThrow(new RegraDeNegocioException("Este link expirou. Solicite um novo."));

        assertThrows(RegraDeNegocioException.class,
                () -> service.redefinir(new RedefinirSenhaDTO("tok-123", "Abcd1234x")));

        verify(usuarioRepository, never()).save(any());
        verify(refreshTokenRepository, never()).deleteByUsuarioId(any());
    }

    @Test
    void podeSolicitar_permiteTresEBloqueiaAQuarta() {
        assertTrue(service.podeSolicitar("Limite@exemplo.com"));
        assertTrue(service.podeSolicitar("limite@exemplo.com"), "mesmo e-mail com caixa diferente entra na mesma janela");
        assertTrue(service.podeSolicitar("limite@exemplo.com"));

        assertFalse(service.podeSolicitar("limite@exemplo.com"), "4ª solicitação na janela deve ser bloqueada");

        verifyNoInteractionsComRepositorios();
    }

    private void verifyNoInteractionsComRepositorios() {
        verify(usuarioRepository, never()).findByEmail(anyString());
        verify(emailService, never()).enviar(anyString(), anyString(), anyString());
        verify(refreshTokenRepository, never()).deleteByUsuarioId(any());
    }
}
