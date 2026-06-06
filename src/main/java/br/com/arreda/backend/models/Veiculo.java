package br.com.arreda.backend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_veiculos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Veiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String placa;

    @Column(nullable = false)
    private String marca;

    @Column(nullable = false)
    private String modelo;

    @Column(nullable = false)
    private String cor;

    @Column(nullable = false)
    private int capacidadePassageiros;

    @ManyToOne
    @JoinColumn(name = "perfil_motorista_id", nullable = false)
    private PerfilMotorista perfilMotorista;

}
