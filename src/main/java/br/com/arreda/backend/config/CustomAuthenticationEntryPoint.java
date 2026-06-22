package br.com.arreda.backend.config;

import br.com.arreda.backend.exception.ErroPadraoDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        // 1. Configura a resposta HTTP como JSON e status 401 Unauthorized
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());

        // 2. Instancia o seu DTO de erro padrão do projeto ARREDA
        ErroPadraoDTO erro = new ErroPadraoDTO(
                "Acesso negado. Voce precisa estar autenticado (JWT ausente, invalido ou expirado).",
                HttpStatus.UNAUTHORIZED.value()
        );

        // 3. Usa o Jackson (ObjectMapper) para converter o objeto Java em texto JSON e escrever na tela
        ObjectMapper mapper = new ObjectMapper();
        response.getWriter().write(mapper.writeValueAsString(erro));
    }
}

