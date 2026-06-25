package br.com.arreda.backend.services;

import br.com.arreda.backend.dto.VeiculoCreateDTO;
import br.com.arreda.backend.exception.RecursoNaoEncontradoException;
import br.com.arreda.backend.exception.RegraDeNegocioException;
import br.com.arreda.backend.dto.VeiculoResponseDTO;
import br.com.arreda.backend.models.PerfilMotorista;
import br.com.arreda.backend.models.Usuario;
import br.com.arreda.backend.models.Veiculo;
import br.com.arreda.backend.repositories.PerfilMotoristaRepository;
import br.com.arreda.backend.repositories.UsuarioRepository;
import br.com.arreda.backend.repositories.VeiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VeiculoService {
    private final VeiculoRepository veiculoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PerfilMotoristaRepository perfilRepository;

    @Transactional
    public Veiculo cadastrarVeiculo(VeiculoCreateDTO dto, String emailUsuarioLogado) {
        // Busca o usuário autenticado
        Usuario usuario = usuarioRepository.findByEmail(emailUsuarioLogado)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));

        //Busca o perfil de motorista do usuário
        PerfilMotorista perfilMotorista = perfilRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new RegraDeNegocioException("Você precisa criar um Perfil de Motorista com CNH antes de cadastrar um veículo."));

        // Placas devem ser únicas no sistema
        if (veiculoRepository.existsByPlaca(dto.placa())) {
            throw new RegraDeNegocioException("Um veículo com esta placa já está cadastrado no sistema.");
        }

        // Monta o objeto veículo mapeando-o para o motorista dono
        Veiculo veiculo = new Veiculo();
        veiculo.setPlaca(dto.placa().toUpperCase().trim()); // padronizado em maiúsculo
        veiculo.setMarca(dto.marca());
        veiculo.setModelo(dto.modelo());
        veiculo.setCor(dto.cor());
        veiculo.setCapacidadePassageiros(dto.capacidadePassageiros());
        veiculo.setPerfilMotorista(perfilMotorista);

        // Salva no banco de dados
        return veiculoRepository.save(veiculo);
    }
    @Transactional(readOnly = true)
    public List<VeiculoResponseDTO> listarVeiculosDoUsuario(Long idUsuaruioAutenticado){

        if(perfilRepository.findByUsuarioId(idUsuaruioAutenticado).isEmpty()){
            throw new RegraDeNegocioException("O usuario nao possui perfil de motorista cadastrado.");
        }

        List<Veiculo> veiculos = veiculoRepository.findAllByPerfilMotoristaUsuarioId(idUsuaruioAutenticado);

        return veiculos.stream().map(veiculo ->
                VeiculoResponseDTO.builder()
                        .id(veiculo.getId())
                        .placa(veiculo.getPlaca())
                        .marca(veiculo.getMarca())
                        .modelo(veiculo.getModelo())
                        .cor(veiculo.getCor())
                        .capacidadePassageiros(veiculo.getCapacidadePassageiros())
                        .build()
        ).collect(Collectors.toList());
    }
}
