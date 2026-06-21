package br.com.arreda.backend.dto;

import java.time.LocalDateTime;

public record CaronaResponseDTO(
        Long id,
        String origem,
        String destino,
        LocalDateTime dataHoraPartida,
        int vagasDisponiveis,
        String motoristaNome,
        String veiculoModelo,
        String veiculoCor
) {}