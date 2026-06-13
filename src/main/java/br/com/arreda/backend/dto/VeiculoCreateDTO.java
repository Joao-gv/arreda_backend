package br.com.arreda.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VeiculoCreateDTO(
        @NotBlank(message = "A placa é obrigatória.")
        @Pattern(regexp = "^[A-Z]{3}[0-9][A-Z0-9][0-9]{2}$", message = "A placa deve seguir o padrão Mercosul (AAA0A00) ou tradicional.")
        String placa,

        @NotBlank(message = "A marca é obrigatória.")
        String marca,

        @NotBlank(message = "O modelo é obrigatório.")
        String modelo,

        @NotBlank(message = "A cor é obrigatória.")
        String cor,

        @Min(value = 1, message = "A capacidade deve ser de pelo menos 1 passageiro.")
        @Max(value = 7, message = "Capacidade máxima sugerida de 7 passageiros.")
        int capacidadePassageiros
) {
}