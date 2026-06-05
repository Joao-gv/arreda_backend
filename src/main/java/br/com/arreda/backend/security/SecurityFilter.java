package br.com.arreda.backend.security;

import br.com.arreda.backend.repositories.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class SecurityFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final UsuarioRepository repository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Recupera o token que veio no cabeçalho (Header) da requisição
        String token = recuperarToken(request);

        if (token != null) {
            // Valida o token e pega o e-mail correspondente
            String email = tokenService.validarToken(token);

            if (!email.isEmpty()) {
                // Se o token for válido, busca o usuário completo no banco
                var usuario = repository.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

                // Cria o objeto de autenticação do Spring Security
                var authentication = new UsernamePasswordAuthenticationToken(usuario, null, Collections.emptyList());

                // Autentica o usuário no "contexto" do Spring (Salva na memória da requisição)
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        // Passa a requisição para frente (para o próximo filtro ou para o seu Controller)
        filterChain.doFilter(request, response);
    }

    private String recuperarToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.replace("Bearer ", "");
    }
}
