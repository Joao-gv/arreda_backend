package br.com.arreda.backend.services;

import br.com.arreda.backend.enums.TipoToken;
import br.com.arreda.backend.exception.RegraDeNegocioException;
import br.com.arreda.backend.models.TokenVerificacao;
import br.com.arreda.backend.models.Usuario;
import br.com.arreda.backend.repositories.TokenVerificacaoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenVerificacaoServiceTest {

    @Mock
    private TokenVerificacaoRepository repository;

    @InjectMocks
    private TokenVerificacaoService service;

    private TokenVerificacao criarToken(LocalDateTime expiraEm, LocalDateTime usadoEm) {
        TokenVerificacao t = new TokenVerificacao();
        t.setToken("abc");
        t.setTipo(TipoToken.CONFIRMACAO_EMAIL);
        t.setUsuario(new Usuario());
        t.setExpiraEm(expiraEm);
        t.setUsadoEm(usadoEm);
        return t;
    }

    @Test
    void tokenValido_deveSerConsumido() {
        TokenVerificacao t = criarToken(LocalDateTime.now().plusHours(1), null);
        when(repository.findByTokenAndTipo("abc", TipoToken.CONFIRMACAO_EMAIL))
                .thenReturn(Optional.of(t));

        Usuario usuario = service.validarEConsumir("abc", TipoToken.CONFIRMACAO_EMAIL);

        assertNotNull(usuario);
        assertNotNull(t.getUsadoEm(), "O token deveria ter sido marcado como usado");
    }

    @Test
    void tokenInexistente_deveLancarErro() {
        when(repository.findByTokenAndTipo(any(), any())).thenReturn(Optional.empty());

        assertThrows(RegraDeNegocioException.class,
                () -> service.validarEConsumir("xyz", TipoToken.CONFIRMACAO_EMAIL));
    }

    @Test
    void tokenExpirado_deveLancarErro() {
        TokenVerificacao t = criarToken(LocalDateTime.now().minusMinutes(1), null);
        when(repository.findByTokenAndTipo(any(), any())).thenReturn(Optional.of(t));

        assertThrows(RegraDeNegocioException.class,
                () -> service.validarEConsumir("abc", TipoToken.CONFIRMACAO_EMAIL));
    }

    @Test
    void tokenJaUsado_deveLancarErro() {
        TokenVerificacao t = criarToken(LocalDateTime.now().plusHours(1),
                LocalDateTime.now().minusMinutes(5));
        when(repository.findByTokenAndTipo(any(), any())).thenReturn(Optional.of(t));

        assertThrows(RegraDeNegocioException.class,
                () -> service.validarEConsumir("abc", TipoToken.CONFIRMACAO_EMAIL));
    }
}