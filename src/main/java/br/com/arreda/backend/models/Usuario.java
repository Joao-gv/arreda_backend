package br.com.arreda.backend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String senha;

    @Column(nullable = false)
    private String telefone;

    // Relacionamento 1:1 Bidirecional.
    //chave estrangeira (usuario_id) fica na tabela de PerfilMotorista, no atributo 'usuario'.
    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL)
    private PerfilMotorista perfilMotorista;
}