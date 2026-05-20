import br.com.arreda.backend.repositories.PerfilMotoristaRepository;
import br.com.arreda.backend.repositories.UsuarioRepository;**
        * Camada de persistência do Arreda! — Data Access Objects (DAO).
        *
        * <p>Este pacote contém as interfaces de repositório que abstraem o acesso
 * ao banco de dados PostgreSQL. Todas as interfaces seguem o padrão
 * {@link org.springframework.data.jpa.repository.JpaRepository} do Spring Data JPA,
        * eliminando a necessidade de implementação manual de SQL para operações comuns.</p>
        *
        * <h2>Repositórios disponíveis</h2>
        * <ul>
 *   <li>{@link UsuarioRepository
} —
        *       acesso à tabela {@code usuario}; inclui validação de e-mail duplicado
 *       e busca por e-mail para o fluxo de autenticação JWT.</li>
        *   <li>{@link PerfilMotoristaRepository
} —
        *       acesso à tabela {@code perfil_motorista}; inclui queries para o
 *       painel de aprovação de CNH pelo administrador.</li>
        * </ul>
        *
        * <h2>Convenção de Query Methods</h2>
        * <p>Os métodos declarados nas interfaces seguem o padrão de nomenclatura do
        * Spring Data JPA ({@code findBy}, {@code existsBy}, {@code findAllBy}),
        * que são traduzidos automaticamente em queries JPQL em tempo de execução.
 * Queries mais complexas utilizam a anotação {@code @Query} com JPQL explícito.</p>
        *
        * <h2>Dependências</h2>
        * <p>As interfaces dependem das entidades do pacote
 * {@code com.arreda.backend.models} e dos enums em
 * {@code com.arreda.backend.models.enums}.</p>
        */
        package com.arreda.backend.repositories;