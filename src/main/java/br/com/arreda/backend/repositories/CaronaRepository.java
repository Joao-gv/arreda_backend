package br.com.arreda.backend.repositories;

import br.com.arreda.backend.models.Carona;
import br.com.arreda.backend.models.ParticipacaoCarona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CaronaRepository extends JpaRepository<Carona, Long> {
    List<Carona> findAllByPerfilMotoristaUsuarioId(Long usuarioId);
}