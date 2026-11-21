package br.com.arreda.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record ExcluirContaDTO(
    @NotBlank(message = "Informe sua senha para confirmar a exclusão.")
    String senha
) {}
