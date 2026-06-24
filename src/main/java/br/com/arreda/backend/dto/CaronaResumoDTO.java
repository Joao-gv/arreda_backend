package br.com.arreda.backend.dto;

import br.com.arreda.backend.enums.StatusCarona;
import br.com.arreda.backend.enums.StatusParticipacao;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;


@Getter
@Setter
@Builder
public class CaronaResumoDTO {
    private Long idCarona;
    private String origem;
    private String destino;
    private LocalDateTime dataHoraPartida;
    private float valorSugerido;
    private StatusCarona statusCarona;
    private String nomeMotorista;
    private StatusParticipacao statusParticipacao; // Será preenchido apenas no papel de Passageiro
}
