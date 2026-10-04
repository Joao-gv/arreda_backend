package br.com.arreda.backend.repositories;

import br.com.arreda.backend.models.TokenVerificacao;
import br.com.arreda.backend.enums.TipoToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenVerificacaoRepository extends JpaRepository<TokenVerificacao, Long> {
    Optional<TokenVerificacao> findByTokenAndTipo(String token, TipoToken tipo);

}
