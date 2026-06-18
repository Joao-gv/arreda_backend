package br.com.arreda.backend.models;

import br.com.arreda.backend.enums.StatusParticipacao;
import br.com.arreda.backend.enums.TipoParticipacao;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tb_participacao_carona")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ParticipacaoCarona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoParticipacao tipo;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private StatusParticipacao status;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "carona_id", nullable = false)
    private Carona carona;
}

