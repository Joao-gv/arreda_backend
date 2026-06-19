package br.com.arreda.backend.repositories;

import br.com.arreda.backend.models.ParticipacaoCarona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParticipacaoCaronaRepository extends JpaRepository<ParticipacaoCarona, Long> {

    boolean existsByUsuarioIdAndCaronaId(Long usuarioId, Long caronaId);

}