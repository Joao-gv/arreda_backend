package br.com.arreda.backend.repositories;

import br.com.arreda.backend.enums.StatusCarona;
import br.com.arreda.backend.enums.StatusParticipacao;
import br.com.arreda.backend.enums.TipoParticipacao;
import br.com.arreda.backend.models.ParticipacaoCarona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import org.springframework.data.repository.query.Param;

@Repository
public interface ParticipacaoCaronaRepository extends JpaRepository<ParticipacaoCarona, Long> {

    boolean existsByUsuarioIdAndCaronaId(Long usuarioId, Long caronaId);
    List<ParticipacaoCarona> findAllByUsuarioId(Long usuarioId);
    List<ParticipacaoCarona> findAllByCaronaIdAndTipo(Long caronaId, TipoParticipacao tipo);


    boolean existsByUsuarioIdAndStatusAndCaronaStatusIn(Long usuarioId, StatusParticipacao status, Collection<StatusCarona> statusCarona);

    @Modifying(flushAutomatically = true)
    @Query("update ParticipacaoCarona p set p.status = :novoStatus " + "where p.usuario.id = :usuarioId and p.status = :statusAtual")
    int atualizarStatusDoUsuario(@Param("usuarioId") Long usuarioId, @Param("statusAtual") StatusParticipacao statusAtual, @Param("novoStatus") StatusParticipacao novoStatus);

}

