package br.com.arreda.backend.services;

import br.com.arreda.backend.dto.CaronaCreateDTO;
import br.com.arreda.backend.enums.StatusCarona;
import br.com.arreda.backend.enums.StatusParticipacao;
import br.com.arreda.backend.enums.TipoParticipacao;
import br.com.arreda.backend.models.*;
import br.com.arreda.backend.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CaronaService {
    private final CaronaRepository caronaRepository;
    private final ParticipacaoCaronaRepository participacaoCaronaRepository;
    private final VeiculoRepository veiculoRepository;
    private final PerfilMotoristaRepository perfilMotoristaRepository;

    @Transactional
    public Carona publicarCarona(CaronaCreateDTO dto, Usuario usuarioLogado) {

        PerfilMotorista motorista = perfilMotoristaRepository.findByUsuarioId(usuarioLogado.getId())
                .orElseThrow(() -> new IllegalArgumentException("Acesso negado: Você precisa ter um perfil de motorista para publicar caronas."));

        // Busca o veiculo e valida se pertence ao motorista
        Veiculo veiculo = veiculoRepository.findById(dto.veiculoId())
                .orElseThrow(() -> new IllegalArgumentException("Erro: Veiculo não encontrado."));

        if (!veiculo.getPerfilMotorista().getId().equals(motorista.getId())) {
            throw new IllegalArgumentException("Erro: o veículo selecionado não pertence ao seu perfil de motorista.");
        }

        // Cria e salva a entidade Carona
        Carona carona = new Carona();
        carona.setOrigem(dto.origem());
        carona.setDestino(dto.destino());
        carona.setDataHoraPartida(dto.datahoraPartida());
        carona.setVagasDisponiveis(dto.vagas());
        carona.setVeiculo(veiculo);


        carona.setPerfilMotorista(motorista);
        carona.setStatus(StatusCarona.ATIVA);

        Carona caronaSalva = caronaRepository.save(carona);

        // Atomicidade: Cria a participação do motorista automaticamente
        ParticipacaoCarona participacao = new ParticipacaoCarona();
        participacao.setCarona(caronaSalva);
        participacao.setUsuario(usuarioLogado);
        participacao.setTipo(TipoParticipacao.MOTORISTA);
        participacao.setStatus(StatusParticipacao.CONFIRMADO);

        participacaoCaronaRepository.save(participacao);

        return caronaSalva;
    }
}