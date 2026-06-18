package br.com.arreda.backend.repositories;

import br.com.arreda.backend.models.Carona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CaronaRepository extends JpaRepository<Carona, Long> {
}