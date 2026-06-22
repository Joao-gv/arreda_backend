package br.com.arreda.backend.repositories;

import br.com.arreda.backend.dto.CaronaResponseDTO;
import br.com.arreda.backend.models.Carona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;

import java.util.List;

@Repository
public interface CaronaRepository extends JpaRepository<Carona, Long> {
    List<Carona> findAllByPerfilMotoristaUsuarioId(Long usuarioId);
    @Query("SELECT c FROM Carona c WHERE c.status = br.com.arreda.backend.enums.StatusCarona.ATIVA " +
            "AND c.vagasDisponiveis > 0 " +
            "AND c.dataHoraPartida >= :agora " +
            "AND (CAST(:origem AS string) IS NULL OR LOWER(c.origem) LIKE LOWER(CONCAT('%', CAST(:origem AS string), '%'))) " +
            "AND (CAST(:destino AS string) IS NULL OR LOWER(c.destino) LIKE LOWER(CONCAT('%', CAST(:destino AS string), '%'))) " +
            "AND (CAST(:dataPartida AS date) IS NULL OR CAST(c.dataHoraPartida AS date) = :dataPartida)")
    Page<Carona> findCaronasDisponiveis(
            @Param("origem") String origem,
            @Param("destino") String destino,
            @Param("dataPartida") LocalDate dataPartida,
            @Param("agora") LocalDateTime agora,
            Pageable pageable
    );
}