package br.com.arreda.backend.repositories;

import br.com.arreda.backend.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Verifica se já existe um usuário cadastrado com o e-mail informado.
     * Mapeado automaticamente pelo Spring Data JPA.
     *
     * @param email E-mail acadêmico a ser verificado
     * @return true se o e-mail já estiver em uso, false caso contrário
     */
    boolean existsByEmail(String email);
}