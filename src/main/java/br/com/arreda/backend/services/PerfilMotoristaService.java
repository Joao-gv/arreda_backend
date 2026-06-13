package br.com.arreda.backend.services;

import br.com.arreda.backend.dto.PerfilMotoristaCreateDTO;
import br.com.arreda.backend.models.PerfilMotorista;
import br.com.arreda.backend.models.Usuario;
import br.com.arreda.backend.repositories.PerfilMotoristaRepository;
import br.com.arreda.backend.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
@RequiredArgsConstructor
public class PerfilMotoristaService {
    private final PerfilMotoristaRepository perfilRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public PerfilMotorista criarPerfil(PerfilMotoristaCreateDTO dto, String emailUsuarioLogado){
    //Busca o usuário no banco de dados usando o e-mail que veio do Token JWT
        Usuario usuario = usuarioRepository.findByEmail(emailUsuarioLogado)
                .orElseThrow(() -> new IllegalArgumentException("Usuario não encontrado."));
    // Regra de negócio: Um usuário só pode ter UM perfil de motorista
        if (perfilRepository.findByUsuarioId(usuario.getId()).isPresent()) {
            throw new IllegalArgumentException("Erro: Você já possui um perfil de motorista cadastrado.");
        }
    //  Monta o objeto
        PerfilMotorista perfil = new PerfilMotorista();
        perfil.setCnh(dto.cnh());
        perfil.setValidadeCnh(dto.validadeCnh());
        perfil.setUsuario(usuario);
    //  Salva no banco
        return perfilRepository.save(perfil);

        }
    }
