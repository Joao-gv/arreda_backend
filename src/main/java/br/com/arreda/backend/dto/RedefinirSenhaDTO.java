package br.com.arreda.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RedefinirSenhaDTO(
        @NotBlank(message = "O token é obrigatório.")
        String token,

        @NotBlank(message = "A nova senha é obrigatória.")
        @Size(min = 8, max = 72, message = "A senha deve conter entre 8 e 72 caracteres.")
        // Regra combinada com o time: letras e números obrigatórios (documentada no README).
        @Pattern(regexp = "(?=.*[A-Za-z])(?=.*\\d).*",
                 message = "A senha deve conter letras e números.")
        String novaSenha
) {}
