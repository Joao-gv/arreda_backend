package br.com.arreda.backend.repositories;

import br.com.arreda.backend.models.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findByUsuarioId(Long usuarioId);

    /** Revoga (apaga) todas as sessões do usuário — usado no fluxo de redefinição de senha. */
    void deleteByUsuarioId(Long usuarioId);
}
