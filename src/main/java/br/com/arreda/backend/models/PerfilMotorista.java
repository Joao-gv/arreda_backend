package br.com.arreda.backend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "tb_perfil_motorista")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerfilMotorista {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String cnh;

    @Column(name = "validade_cnh", nullable = false)
    private LocalDate validadeCnh;

    @Column(name = "status_validacao")
    private String statusValidacao;

    // Relacionamento 1:1 onde ESTA tabela é a dona da chave estrangeira
    @OneToOne
    @JoinColumn(name = "usuario_id", referencedColumnName = "id")
    private Usuario usuario;
}