package br.com.arreda.backend.services;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TemplateEmailTest {

    @Test
    void substituiMarcadoresEEscapaHtmlDoNome() {
        String html = TemplateEmail.renderizar("/templates/email-confirmacao.html", Map.of(
                "nome", "<b>Maria</b> & Cia",
                "linkConfirmacao", "http://localhost:5173/confirmar-email?token=abc"));

        assertTrue(html.contains("&lt;b&gt;Maria&lt;/b&gt; &amp; Cia"));
        assertFalse(html.contains("<b>Maria</b>"), "o nome não pode injetar marcação no e-mail");
        assertTrue(html.contains("href=\"http://localhost:5173/confirmar-email?token=abc\""));
        assertFalse(html.contains("{{"));
    }

    @Test
    void templateInexistenteLancaErroClaro() {
        assertThrows(IllegalStateException.class,
                () -> TemplateEmail.renderizar("/templates/nao-existe.html", Map.of()));
    }

    @Test
    void linkTemExatamenteUmaBarraEntreBaseECaminho() {
        assertEquals("http://x.com/a?token=1", TemplateEmail.link("http://x.com", "a?token=1"));
        assertEquals("http://x.com/a?token=1", TemplateEmail.link("http://x.com/", "a?token=1"));
    }
}
