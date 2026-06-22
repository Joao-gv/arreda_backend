package br.com.arreda.backend.repositories;

import br.com.arreda.backend.models.Veiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VeiculoRepository extends JpaRepository<Veiculo, Long> {
    boolean existsByPlaca(String placa);
    List<Veiculo> findAllByPerfilMotoristaUsuarioId(Long usuarioId);
}