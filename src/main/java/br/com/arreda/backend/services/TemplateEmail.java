package br.com.arreda.backend.services;

import org.springframework.web.util.HtmlUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/** Carrega um template HTML de e-mail e substitui os marcadores {{chave}} com os valores escapados. */
public final class TemplateEmail {

    private TemplateEmail() {
    }

    /**
     * @param caminho   caminho no classpath, ex.: "/templates/email-confirmacao.html"
     * @param variaveis marcador (sem chaves) → valor; os valores são escapados para HTML,
     *                  evitando que dados do usuário (ex.: nome) injetem marcação no e-mail.
     */
    public static String renderizar(String caminho, Map<String, String> variaveis) {
        try (InputStream entrada = TemplateEmail.class.getResourceAsStream(caminho)) {
            if (entrada == null) {
                throw new IOException("Template não encontrado: " + caminho);
            }
            String html = new String(entrada.readAllBytes(), StandardCharsets.UTF_8);
            for (Map.Entry<String, String> variavel : variaveis.entrySet()) {
                html = html.replace("{{" + variavel.getKey() + "}}", HtmlUtils.htmlEscape(variavel.getValue(), StandardCharsets.UTF_8.name()));
            }
            return html;
        } catch (IOException excecao) {
            throw new IllegalStateException("Falha ao montar o e-mail a partir do template " + caminho + ".", excecao);
        }
    }

    /** Monta o link do frontend garantindo uma única barra entre a base e o caminho. */
    public static String link(String frontendUrl, String caminhoComQuery) {
        String base = frontendUrl.endsWith("/") ? frontendUrl : frontendUrl + "/";
        return base + caminhoComQuery;
    }
}
