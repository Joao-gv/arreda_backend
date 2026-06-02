package br.com.arreda.backend.security;

import br.com.arreda.backend.models.Usuario;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class TokenService {

    @Value("${api.security.token.secret}")
    private String secret;

    public String gerarToken(Usuario usuario) {
        try {
            Algorithm algoritmo = Algorithm.HMAC256(secret);
            return JWT.create()
                    .withIssuer("arreda_backend") // Quem gerou o token
                    .withSubject(usuario.getEmail()) // Identificação do usuário
                    .withExpiresAt(gerarDataExpiracao()) // Duração de 2 horas
                    .sign(algoritmo);
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Erro ao gerar token JWT", exception);
        }
    }

    private Instant gerarDataExpiracao() {
        // Pega a hora atual, soma 2 horas e converte para o fuso horário de Brasília (-03:00)
        return Instant.now().plusSeconds(7200);
    }
}