package br.com.arreda.backend.security;


import br.com.arreda.backend.models.RefreshToken;
import br.com.arreda.backend.repositories.RefreshTokenRepository;
import br.com.arreda.backend.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public RefreshToken criarRefreshToken(Long usuarioId){
        var usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
        RefreshToken refreshToken = refreshTokenRepository.findByUsuarioId(usuario.getId())
                .orElse(new RefreshToken());

        refreshToken.setUsuario(usuario);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setDataExpiracao(LocalDateTime.now().plusDays(7));

        return refreshTokenRepository.save(refreshToken);

    }

    public RefreshToken verificarExpiracao(RefreshToken token) {
        if (token.getDataExpiracao().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(token); // Limpa do banco se estiver expirado
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Refresh token expirado. Faça login novamente.");
        }
        return token;
    }

    public Optional<RefreshToken> buscarPorToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

}
