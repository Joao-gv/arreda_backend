package br.com.arreda.backend.dto;

// DTO para devolver a dupla de tokens na resposta
public record TokenResponseDTO(String accessToken, String refreshToken) {}