package br.com.arreda.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UsuarioUpdateDTO(
        @NotBlank(message = "O nome é obrigatório.")
        @Size(min = 2, max = 100, message = "O nome deve conter entre 2 e 100 caracteres.")
        String nome,

        @NotBlank(message = "O telefone é obrigatório.")
        @Pattern(regexp = "\\d{10,15}", message = "O telefone deve conter apenas números, entre 10 e 15 dígitos.")
        String telefone
) {}