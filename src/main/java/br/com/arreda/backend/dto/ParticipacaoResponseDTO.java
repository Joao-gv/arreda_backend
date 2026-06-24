package br.com.arreda.backend.dto;

import br.com.arreda.backend.enums.StatusParticipacao;

public record ParticipacaoResponseDTO(
    Long idParticipacao,
    String nomePassageiro,
    StatusParticipacao status
) {}
