package br.com.arreda.backend.dto;

public record UsuarioMeDTO(
    Long id,
    String nome,
    String email,
    String telefone,
    boolean ehMotorista
) {}
