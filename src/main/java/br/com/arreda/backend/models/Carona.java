package br.com.arreda.backend.models;

import br.com.arreda.backend.enums.StatusCarona;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_carona")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Carona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String origem;

    @Column(nullable = false)
    private String destino;

    @Column(nullable = false)
    private LocalDateTime dataHoraPartida;

    @Column(nullable = false)
    private float valorSugerido;

    @Column(nullable = false)
    private int vagasDisponiveis;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private StatusCarona status;

    @ManyToOne
    @JoinColumn(name = "perfil_motorista_id", nullable = false)
    private PerfilMotorista perfilMotorista;

    @ManyToOne
    @JoinColumn(name = "veiculo_id", nullable = false)
    private Veiculo veiculo;

}
