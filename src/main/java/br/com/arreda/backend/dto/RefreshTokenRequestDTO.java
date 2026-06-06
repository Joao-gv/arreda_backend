package br.com.arreda.backend.dto;

import jakarta.validation.constraints.NotBlank;

// DTO para receber o token antigo na rota /refresh
public record RefreshTokenRequestDTO(@NotBlank String refreshToken) {}