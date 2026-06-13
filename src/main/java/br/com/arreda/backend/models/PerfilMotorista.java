package br.com.arreda.backend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
    @ToString.Exclude
    private Usuario usuario;
    @OneToMany(mappedBy = "perfilMotorista", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Veiculo> veiculos = new ArrayList<>();
}