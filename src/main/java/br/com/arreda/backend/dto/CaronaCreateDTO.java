package br.com.arreda.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record CaronaCreateDTO(
        @NotBlank(message = "A origem não pode estar em branco")
        String origem,

        @NotBlank(message = "O destino não pode estar em branco")
        String destino,

        @NotNull(message = "A data e hora da partida são obrigatórias")
        LocalDateTime datahoraPartida,

        @NotNull(message = "O número de vagas é obrigatório")
        Integer vagas,

        @NotNull(message = "O ID do veículo é obrigatório")
        Long veiculoId
) {}