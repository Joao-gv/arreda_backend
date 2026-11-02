package br.com.arreda.backend.controllers;

import br.com.arreda.backend.enums.TipoToken;
import br.com.arreda.backend.models.TokenVerificacao;
import br.com.arreda.backend.repositories.TokenVerificacaoRepository;
import br.com.arreda.backend.repositories.UsuarioRepository;
import br.com.arreda.backend.services.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Critérios de aceite da S7-03 na rota real: cadastro → e-mail com link → login bloqueado (403)
 * → confirmação → login liberado; token adulterado; reenvio sem revelar cadastros.
 * O EmailService é mockado; os dados criados são revertidos por @Transactional.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ConfirmacaoEmailEndpointTest {

    private static final String SENHA = "senha123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarios;

    @Autowired
    private TokenVerificacaoRepository tokens;

    @MockitoBean
    private EmailService emailService;

    private String novoEmail() {
        return "confirmacao-" + UUID.randomUUID() + "@test.local";
    }

    private MvcResult chamar(String rota, String json) throws Exception {
        return mockMvc.perform(post(rota)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)).andReturn();
    }

    private void cadastrar(String email) throws Exception {
        assertEquals(201, chamar("/api/usuarios/register", "{\"nome\":\"Teste Confirmacao\",\"email\":\"" + email
                + "\",\"senha\":\"" + SENHA + "\",\"telefone\":\"31999990000\"}").getResponse().getStatus());
    }

    private String tokenConfirmacaoEmAberto(String email) {
        Long usuarioId = usuarios.findByEmail(email).orElseThrow().getId();
        return tokens.findAll().stream()
                .filter(t -> t.getTipo() == TipoToken.CONFIRMACAO_EMAIL && t.getUsadoEm() == null)
                .filter(t -> t.getUsuario().getId().equals(usuarioId))
                .map(TokenVerificacao::getToken)
                .findFirst()
                .orElseThrow(() -> new AssertionError("token CONFIRMACAO_EMAIL em aberto não encontrado"));
    }

    private String login(String email) {
        return "{\"email\":\"" + email + "\",\"senha\":\"" + SENHA + "\"}";
    }

    @Test
    void fluxoCompleto_cadastroLoginBloqueadoConfirmacaoLoginLiberado() throws Exception {
        String email = novoEmail();
        cadastrar(email);

        String token = tokenConfirmacaoEmAberto(email);
        verify(emailService).enviar(eq(email), contains("Confirme"), contains("confirmar-email?token=" + token));
        assertFalse(usuarios.findByEmail(email).orElseThrow().isEmailVerificado());

        // login antes de confirmar: 403 com código específico
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(login(email)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("EMAIL_NAO_VERIFICADO"));

        // senha errada continua 401 (não revela que a conta existe e não foi verificada)
        assertEquals(401, chamar("/api/auth/login",
                "{\"email\":\"" + email + "\",\"senha\":\"errada999\"}").getResponse().getStatus());

        // confirmação
        mockMvc.perform(post("/api/auth/confirmar-email")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"token\":\"" + token + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensagem").exists());
        assertTrue(usuarios.findByEmail(email).orElseThrow().isEmailVerificado());

        // login liberado
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(login(email)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists());

        // reutilizar o link: 400 amigável
        mockMvc.perform(post("/api/auth/confirmar-email")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"token\":\"" + token + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Este link já foi utilizado."));
    }

    @Test
    void tokenAdulterado_retorna400AmigavelSemStackTrace() throws Exception {
        String email = novoEmail();
        cadastrar(email);
        String adulterado = tokenConfirmacaoEmAberto(email) + "x";

        MvcResult resultado = chamar("/api/auth/confirmar-email", "{\"token\":\"" + adulterado + "\"}");

        assertEquals(400, resultado.getResponse().getStatus());
        String corpo = resultado.getResponse().getContentAsString();
        assertTrue(corpo.contains("\"erro\""));
        assertFalse(corpo.contains("Exception"), "não pode vazar stack trace");
        assertFalse(corpo.contains("at br.com"), "não pode vazar stack trace");
        assertFalse(usuarios.findByEmail(email).orElseThrow().isEmailVerificado());
    }

    @Test
    void tokenAusente_retorna400PorCampo() throws Exception {
        mockMvc.perform(post("/api/auth/confirmar-email")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"token\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].campo").value("token"));
    }

    @Test
    void reenviar_respostaIdenticaParaEmailExistenteEInexistente_eLimiteDe2Minutos() throws Exception {
        String email = novoEmail();
        cadastrar(email);

        MvcResult existente = chamar("/api/auth/reenviar-confirmacao", "{\"email\":\"" + email + "\"}");
        MvcResult inexistente = chamar("/api/auth/reenviar-confirmacao", "{\"email\":\"" + novoEmail() + "\"}");

        assertEquals(200, existente.getResponse().getStatus());
        assertEquals(200, inexistente.getResponse().getStatus());
        assertEquals(existente.getResponse().getContentAsString(), inexistente.getResponse().getContentAsString());

        // cadastro + 1 reenvio
        verify(emailService, times(2)).enviar(eq(email), contains("Confirme"), anyString());

        // segundo pedido dentro de 2 minutos: 429
        assertEquals(429, chamar("/api/auth/reenviar-confirmacao",
                "{\"email\":\"" + email + "\"}").getResponse().getStatus());
    }

    @Test
    void reenviar_usarONovoLinkInvalidaOAnterior() throws Exception {
        String email = novoEmail();
        cadastrar(email);
        String primeiro = tokenConfirmacaoEmAberto(email);
        chamar("/api/auth/reenviar-confirmacao", "{\"email\":\"" + email + "\"}");
        Long usuarioId = usuarios.findByEmail(email).orElseThrow().getId();
        String segundo = tokens.findAll().stream()
                .filter(t -> t.getTipo() == TipoToken.CONFIRMACAO_EMAIL && t.getUsadoEm() == null)
                .filter(t -> t.getUsuario().getId().equals(usuarioId))
                .map(TokenVerificacao::getToken)
                .filter(t -> !t.equals(primeiro))
                .findFirst().orElseThrow();

        assertEquals(200, chamar("/api/auth/confirmar-email",
                "{\"token\":\"" + segundo + "\"}").getResponse().getStatus());
        assertEquals(400, chamar("/api/auth/confirmar-email",
                "{\"token\":\"" + primeiro + "\"}").getResponse().getStatus());
    }
}
