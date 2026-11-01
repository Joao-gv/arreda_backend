package br.com.arreda.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record ConfirmarEmailDTO(
        @NotBlank(message = "O token é obrigatório.")
        String token
) {}
