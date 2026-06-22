package br.com.arreda.backend.services;

import br.com.arreda.backend.dto.CaronaCreateDTO;
import br.com.arreda.backend.dto.CaronaResumoDTO;
import br.com.arreda.backend.dto.HistoricoCaronaDTO;
import br.com.arreda.backend.enums.StatusCarona;
import br.com.arreda.backend.enums.StatusParticipacao;
import br.com.arreda.backend.enums.TipoParticipacao;
import br.com.arreda.backend.exception.RecursoNaoEncontradoException;
import br.com.arreda.backend.exception.RegraDeNegocioException;
import br.com.arreda.backend.models.*;
import br.com.arreda.backend.dto.CaronaResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import br.com.arreda.backend.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

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
                .orElseThrow(() -> new RegraDeNegocioException(
                        "Você precisa ter um perfil de motorista para publicar caronas."));

        Veiculo veiculo = veiculoRepository.findById(dto.veiculoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Veiculo não encontrado."));

        if (!veiculo.getPerfilMotorista().getId().equals(motorista.getId())) {
            throw new RegraDeNegocioException(
                    "O veículo selecionado não pertence ao seu perfil de motorista.");
        }

        if (veiculo.getCapacidadePassageiros() < dto.vagas()) {
            throw new RegraDeNegocioException(
                    "O número de vagas informado é maior que a do veículo.");
        }

        Carona carona = new Carona();
        carona.setOrigem(dto.origem());
        carona.setDestino(dto.destino());
        carona.setDataHoraPartida(dto.datahoraPartida());
        carona.setVagasDisponiveis(dto.vagas());
        carona.setVeiculo(veiculo);

        carona.setPerfilMotorista(motorista);
        carona.setStatus(StatusCarona.ATIVA);

        Carona caronaSalva = caronaRepository.save(carona);

        ParticipacaoCarona participacao = new ParticipacaoCarona();
        participacao.setCarona(caronaSalva);
        participacao.setUsuario(usuarioLogado);
        participacao.setTipo(TipoParticipacao.MOTORISTA);
        participacao.setStatus(StatusParticipacao.CONFIRMADO);

        participacaoCaronaRepository.save(participacao);

        return caronaSalva;
    }

    @Transactional
    public ParticipacaoCarona solicitarParticipacao(Long caronaId, Usuario usuarioLogado) {

        Carona carona = caronaRepository.findById(caronaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Carona não encontrada."));

        if (carona.getStatus() != StatusCarona.ATIVA) {
            throw new RegraDeNegocioException(
                    "A carona não está disponível para solicitações.");
        }

        if (carona.getVagasDisponiveis() <= 0) {
            throw new RegraDeNegocioException("Não há vagas disponíveis.");
        }

        if (carona.getPerfilMotorista().getUsuario().getId()
                .equals(usuarioLogado.getId())) {
            throw new RegraDeNegocioException(
                    "Você não pode solicitar participação na sua própria carona.");
        }

        boolean jaParticipa = participacaoCaronaRepository
                .existsByUsuarioIdAndCaronaId(
                        usuarioLogado.getId(),
                        caronaId
                );

        if (jaParticipa) {
            throw new RegraDeNegocioException(
                    "Você já possui uma solicitação ou participação nesta carona.");
        }

        ParticipacaoCarona participacao = new ParticipacaoCarona();

        participacao.setCarona(carona);
        participacao.setUsuario(usuarioLogado);
        participacao.setTipo(TipoParticipacao.PASSAGEIRO);
        participacao.setStatus(StatusParticipacao.PENDENTE);

        return participacaoCaronaRepository.save(participacao);
    }

    @Transactional
    public void aceitarPassageiro(Long idCarona, Long idParticipacao, Long idUsuarioAutenticado) {

        Carona carona = caronaRepository.findById(idCarona)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Carona não encontrada."));

        Long idMotoristaDaCarona = carona.getPerfilMotorista().getUsuario().getId();
        if (!idMotoristaDaCarona.equals(idUsuarioAutenticado)) {
            throw new RegraDeNegocioException("Apenas o motorsta desta carona pode aceitar passageiros.");
        }

        ParticipacaoCarona participacao = participacaoCaronaRepository.findById(idParticipacao)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Solicitação de carona não encontrada."));

        if (!participacao.getCarona().getId().equals(idCarona)) {
            throw new RegraDeNegocioException("Esta solicitação não pertence à carona informada.");
        }

        if (carona.getVagasDisponiveis() <= 0) {
            throw new RegraDeNegocioException("Não há mais vagas disponíveis nesta carona.");
        }

        if (participacao.getStatus() != StatusParticipacao.PENDENTE) {
            throw new RegraDeNegocioException("Esta solicitação já foi respondida anteriormente.");
        }

        participacao.setStatus(StatusParticipacao.CONFIRMADO);
        participacaoCaronaRepository.save(participacao);

        carona.setVagasDisponiveis(carona.getVagasDisponiveis() - 1);

        if (carona.getVagasDisponiveis() == 0) {
            carona.setStatus(StatusCarona.LOTADA);
        }

        caronaRepository.save(carona);
    }

    @Transactional
    public void rejeitarPassageiro(Long idCarona, Long idParticipacao, Long idUsuarioAutenticado) {

        Carona carona = caronaRepository.findById(idCarona)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Carona não encontrada."));

        // Valida se quem está rejeitando é o motorista
        Long idMotoristaDaCarona = carona.getPerfilMotorista().getUsuario().getId();
        if (!idMotoristaDaCarona.equals(idUsuarioAutenticado)) {
            throw new RegraDeNegocioException("Apenas o motorista desta carona pode rejeitar passageiros.");
        }

        ParticipacaoCarona participacao = participacaoCaronaRepository.findById(idParticipacao)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Solicitação de participação não encontrada."));

        if (!participacao.getCarona().getId().equals(idCarona)) {
            throw new RecursoNaoEncontradoException("Esta solicitação não pertence à carona informada.");
        }

        if (participacao.getStatus() != StatusParticipacao.PENDENTE) {
            throw new RegraDeNegocioException("Esta solicitação já foi respondida anteriormente.");
        }

        // Altera o status para REJEITADO (Aqui as vagas da carona NÃO se alteram)
        participacao.setStatus(StatusParticipacao.REJEITADO);
        participacaoCaronaRepository.save(participacao);
    }

    @Transactional(readOnly = true) // Boa prática para consultas: melhora a performance
    public HistoricoCaronaDTO buscarHistoricoUsuario(Long idUsuarioAutenticado) {

        // 1. Busca caronas onde o usuário é o MOTORISTA
        List<Carona> caronasMotorista = caronaRepository.findAllByPerfilMotoristaUsuarioId(idUsuarioAutenticado);

        List<CaronaResumoDTO> listaComoMotorista = caronasMotorista.stream().map(carona ->
                CaronaResumoDTO.builder()
                        .idCarona(carona.getId())
                        .origem(carona.getOrigem())
                        .destino(carona.getDestino())
                        .dataHoraPartida(carona.getDataHoraPartida())
                        .statusCarona(carona.getStatus())
                        .nomeMotorista(carona.getPerfilMotorista().getUsuario().getNome()) // Ele mesmo
                        .statusParticipacao(null) // Motorista não tem status de participação
                        .build()
        ).collect(Collectors.toList());

        // 2. Busca caronas onde o usuário é PASSAGEIRO (via tabela de participação)
        List<ParticipacaoCarona> participacoesPassageiro = participacaoCaronaRepository.findAllByUsuarioId(idUsuarioAutenticado);

        List<CaronaResumoDTO> listaComoPassageiro = participacoesPassageiro.stream()
                .filter(p -> !p.getCarona().getPerfilMotorista().getUsuario().getId().equals(idUsuarioAutenticado))
                .map(participacao -> {
            Carona carona = participacao.getCarona(); // Pega a carona associada àquela participação
            return CaronaResumoDTO.builder()
                    .idCarona(carona.getId())
                    .origem(carona.getOrigem())
                    .destino(carona.getDestino())
                    .dataHoraPartida(carona.getDataHoraPartida())
                    .statusCarona(carona.getStatus())
                    .nomeMotorista(carona.getPerfilMotorista().getUsuario().getNome()) // Nome do dono do carro
                    .statusParticipacao(participacao.getStatus()) // PENDENTE, CONFIRMADO, etc.
                    .build();
        }).collect(Collectors.toList());

        // 3. Retorna o DTO mestre com as duas listas separadas
        return new HistoricoCaronaDTO(listaComoMotorista, listaComoPassageiro);
    }
    @Transactional(readOnly = true)
    public Page<CaronaResponseDTO> buscarCaronas(String origem, String destino, LocalDate data, Pageable pageable) {
        LocalDateTime agora = LocalDateTime.now();

        Page<Carona> caronasPage = caronaRepository.findCaronasDisponiveis(origem, destino, data, agora, pageable);

        // Mapeia a entidade para o DTO higienizado
        return caronasPage.map(carona -> {
            String nomeCompleto = carona.getPerfilMotorista().getUsuario().getNome();
            String primeiroNome = (nomeCompleto != null && nomeCompleto.contains(" "))
                    ? nomeCompleto.split(" ")[0]
                    : nomeCompleto;

            return new CaronaResponseDTO(
                    carona.getId(),
                    carona.getOrigem(),
                    carona.getDestino(),
                    carona.getDataHoraPartida(),
                    carona.getVagasDisponiveis(),
                    primeiroNome,
                    carona.getVeiculo().getModelo(),
                    carona.getVeiculo().getCor()
            );
        });
    }
}
