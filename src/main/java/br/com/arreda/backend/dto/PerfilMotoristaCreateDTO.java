package br.com.arreda.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;


public record PerfilMotoristaCreateDTO(
        @NotBlank(message = "A CNH é obrigatória.")
        String cnh,

        @NotNull(message = "A data de validade da CNH é obrigatória.")
        LocalDate validadeCnh

) {}
