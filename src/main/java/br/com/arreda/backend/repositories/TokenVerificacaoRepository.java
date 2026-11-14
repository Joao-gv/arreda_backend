package br.com.arreda.backend.repositories;

import br.com.arreda.backend.models.TokenVerificacao;
import br.com.arreda.backend.enums.TipoToken;
import br.com.arreda.backend.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TokenVerificacaoRepository extends JpaRepository<TokenVerificacao, Long> {
    Optional<TokenVerificacao> findByTokenAndTipo(String token, TipoToken tipo);

    /**
     * Marca como usados todos os tokens ainda não usados do usuário para o tipo informado.
     * flush/clear evitam que o contexto de persistência devolva depois um token desatualizado da memória.
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update TokenVerificacao t set t.usadoEm = :agora "
            + "where t.usuario = :usuario and t.tipo = :tipo and t.usadoEm is null")
    int invalidarPendentes(@Param("usuario") Usuario usuario,
                           @Param("tipo") TipoToken tipo,
                           @Param("agora") LocalDateTime agora);
}
