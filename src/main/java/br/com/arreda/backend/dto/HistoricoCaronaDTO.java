package br.com.arreda.backend.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class HistoricoCaronaDTO {
    private List<CaronaResumoDTO> comoMotorista;
    private List<CaronaResumoDTO> comoPassageiro;
}
