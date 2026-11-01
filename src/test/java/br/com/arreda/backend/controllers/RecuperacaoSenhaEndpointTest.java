package br.com.arreda.backend.controllers;

import br.com.arreda.backend.enums.TipoToken;
import br.com.arreda.backend.models.TokenVerificacao;
import br.com.arreda.backend.models.Usuario;
import br.com.arreda.backend.repositories.RefreshTokenRepository;
import br.com.arreda.backend.repositories.TokenVerificacaoRepository;
import br.com.arreda.backend.repositories.UsuarioRepository;
import br.com.arreda.backend.security.RefreshTokenService;
import br.com.arreda.backend.services.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Cobertura de aceite do fluxo "esqueci-senha -> redefinir-senha" na rota real (seam HTTP).
 * O EmailService é mockado: o disparo real de e-mail já é coberto pela task S7-01;
 * os asserts focam no contrato HTTP e nos efeitos de banco, conforme a task S7-05.
 * O usuário de teste criado via repository é revertido por @Transactional.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RecuperacaoSenhaEndpointTest {

    private static final String MENSAGEM_PADRAO =
            "Se o e-mail estiver cadastrado, você receberá um link de redefinição.";
    private static final String SENHA_ANTIGA = "senha-antiga-1";
    private static final String SENHA_NOVA = "NovaSenha1";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarios;

    @Autowired
    private TokenVerificacaoRepository tokens;

    @Autowired
    private RefreshTokenRepository refreshTokens;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private PasswordEncoder encoder;

    @MockitoBean
    private EmailService emailService;

    private String email;
    private Long usuarioId;

    @BeforeEach
    void criarUsuario() {
        email = "recuperacao-" + UUID.randomUUID() + "@test.local";
        Usuario usuario = new Usuario();
        usuario.setNome("Usuario Senha");
        usuario.setEmail(email);
        usuario.setSenhaHash(encoder.encode(SENHA_ANTIGA));
        usuario.setTelefone("31900000009");
        usuario.setEmailVerificado(true); // login agora exige e-mail confirmado (S7-03)
        usuarios.saveAndFlush(usuario);
        usuarioId = usuario.getId();
    }

    private MvcResult postEsqueci(String umEmail) throws Exception {
        return mockMvc.perform(post("/api/auth/esqueci-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + umEmail + "\"}"))
                .andReturn();
    }

    @Test
    void esqueciSenha_respostaIdenticaParaEmailExistenteEInexistente() throws Exception {
        MvcResult existente = postEsqueci(email);
        MvcResult inexistente = postEsqueci("fantasma-" + UUID.randomUUID() + "@test.local");

        String corpoExistente = existente.getResponse().getContentAsString();
        String corpoInexistente = inexistente.getResponse().getContentAsString();

        assertEquals(200, existente.getResponse().getStatus());
        assertEquals(200, inexistente.getResponse().getStatus());
        assertEquals(corpoExistente, corpoInexistente,
                "e-mail existente e inexistente devem ter a mesma resposta");
        assertTrue(corpoExistente.contains(MENSAGEM_PADRAO));

        verify(emailService, times(1)).enviar(anyString(), anyString(), anyString());
        verify(emailService).enviar(eq(email), contains("Redefini"), contains("redefinir-senha?token="));
    }

    @Test
    void esqueciSenha_quartaSolicitacaoNaJanelaRetorna429() throws Exception {
        String emailLimite = "limite-" + UUID.randomUUID() + "@test.local";

        assertEquals(200, postEsqueci(emailLimite).getResponse().getStatus());
        assertEquals(200, postEsqueci(emailLimite).getResponse().getStatus());
        assertEquals(200, postEsqueci(emailLimite).getResponse().getStatus());

        MvcResult quarta = postEsqueci(emailLimite);
        assertEquals(429, quarta.getResponse().getStatus(),
                "4ª solicitação na janela deve retornar 429");
        assertTrue(quarta.getResponse().getContentAsString().contains("Muitas tentativas"));

        verifyNoInteractions(emailService);
    }

    private String tokenResetEmAberto() {
        return tokens.findAll().stream()
                .filter(t -> t.getTipo() == TipoToken.RESET_SENHA)
                .filter(t -> t.getUsadoEm() == null)
                .filter(t -> t.getUsuario().getId().equals(usuarioId))
                .map(TokenVerificacao::getToken)
                .findFirst()
                .orElseThrow(() -> new AssertionError("token RESET_SENHA em aberto não encontrado"));
    }

    private int statusLogin(String umEmail, String senha) throws Exception {
        return mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + umEmail + "\",\"senha\":\"" + senha + "\"}"))
                .andReturn().getResponse().getStatus();
    }

    @Test
    void redefinirSenha_fluxoCompleto() throws Exception {
        refreshTokenService.criarRefreshToken(usuarioId);
        String tokenSessaoAntiga = refreshTokens.findByUsuarioId(usuarioId).orElseThrow().getToken();

        assertEquals(200, postEsqueci(email).getResponse().getStatus());
        String token = tokenResetEmAberto();

        // senha fraca: 400 com erro por campo e token NÃO consumido
        mockMvc.perform(post("/api/auth/redefinir-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"" + token + "\",\"novaSenha\":\"abc\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].campo").value("novaSenha"));
        assertEquals(token, tokenResetEmAberto(), "validação de campo não deve consumir o token");

        // redefinição válida
        mockMvc.perform(post("/api/auth/redefinir-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"" + token + "\",\"novaSenha\":\"" + SENHA_NOVA + "\"}"))
                .andExpect(status().isOk());

        // sessões antigas revogadas (antes dos novos logins, que criam nova sessão)
        assertTrue(refreshTokens.findByUsuarioId(usuarioId).isEmpty(),
                "refresh tokens do usuário devem ser apagados na redefinição");
        assertTrue(refreshTokens.findByToken(tokenSessaoAntiga).isEmpty(),
                "o valor da sessão antiga deve deixar de existir no banco");

        // senha antiga falha, nova funciona
        assertEquals(401, statusLogin(email, SENHA_ANTIGA));
        assertEquals(200, statusLogin(email, SENHA_NOVA));

        // token reutilizado: 400
        mockMvc.perform(post("/api/auth/redefinir-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"" + token + "\",\"novaSenha\":\"OutraSenha2\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void redefinirSenha_usarUmLinkInvalidaOsLinksAnterioresPendentes() throws Exception {
        assertEquals(200, postEsqueci(email).getResponse().getStatus());
        String primeiro = tokenResetEmAberto();
        assertEquals(200, postEsqueci(email).getResponse().getStatus());
        String segundo = tokens.findAll().stream()
                .filter(t -> t.getTipo() == TipoToken.RESET_SENHA && t.getUsadoEm() == null)
                .filter(t -> t.getUsuario().getId().equals(usuarioId))
                .map(TokenVerificacao::getToken)
                .filter(t -> !t.equals(primeiro))
                .findFirst().orElseThrow();

        mockMvc.perform(post("/api/auth/redefinir-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"" + segundo + "\",\"novaSenha\":\"" + SENHA_NOVA + "\"}"))
                .andExpect(status().isOk());

        // o link do primeiro pedido não pode mais trocar a senha
        mockMvc.perform(post("/api/auth/redefinir-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"" + primeiro + "\",\"novaSenha\":\"OutraSenha2\"}"))
                .andExpect(status().isBadRequest());
        assertEquals(200, statusLogin(email, SENHA_NOVA));
    }

    @Test
    void redefinirSenha_comTokenExpiradoRetorna400() throws Exception {
        TokenVerificacao expirado = new TokenVerificacao();
        expirado.setToken("exp-" + UUID.randomUUID());
        expirado.setUsuario(usuarios.findById(usuarioId).orElseThrow());
        expirado.setTipo(TipoToken.RESET_SENHA);
        expirado.setExpiraEm(LocalDateTime.now().minusMinutes(1));
        tokens.saveAndFlush(expirado);

        mockMvc.perform(post("/api/auth/redefinir-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"" + expirado.getToken() + "\",\"novaSenha\":\"" + SENHA_NOVA + "\"}"))
                .andExpect(status().isBadRequest());
    }
}
