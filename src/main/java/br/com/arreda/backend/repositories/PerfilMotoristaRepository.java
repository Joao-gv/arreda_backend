package br.com.arreda.backend.repositories;

import br.com.arreda.backend.models.PerfilMotorista;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PerfilMotoristaRepository extends JpaRepository<PerfilMotorista, Long> {
    // Herda todas as operações de CRUD básicas (save, findById, delete, etc.)
    Optional<PerfilMotorista> findByUsuarioId(long usuarioId);
}