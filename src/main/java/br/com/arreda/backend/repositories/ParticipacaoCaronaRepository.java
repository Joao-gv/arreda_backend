package br.com.arreda.backend.repositories;

import br.com.arreda.backend.enums.TipoParticipacao;
import br.com.arreda.backend.models.ParticipacaoCarona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParticipacaoCaronaRepository extends JpaRepository<ParticipacaoCarona, Long> {

    boolean existsByUsuarioIdAndCaronaId(Long usuarioId, Long caronaId);
    List<ParticipacaoCarona> findAllByUsuarioId(Long usuarioId);
    List<ParticipacaoCarona> findAllByCaronaIdAndTipo(Long caronaId, TipoParticipacao tipo);
}