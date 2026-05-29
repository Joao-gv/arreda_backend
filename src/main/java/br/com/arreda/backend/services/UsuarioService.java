package br.com.arreda.backend.services;

import br.com.arreda.backend.dto.UsuarioCreateDTO;
import br.com.arreda.backend.models.Usuario;
import br.com.arreda.backend.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder; // Escrito corretamente e injetado pelo Lombok

    @Transactional
    public Usuario salvarUsuario(UsuarioCreateDTO dto) {

        // 1. Formata o e-mail para minúsculas
        String emailFormatado = dto.getEmail().toLowerCase().trim();

        // 2. Verifica se o e-mail já existe
        if (repository.existsByEmail(emailFormatado)) {
            throw new IllegalArgumentException("Erro: O e-mail informado já está cadastrado.");
        }

        // 3. Criptografa a senha
        String senhaCriptografada = passwordEncoder.encode(dto.getSenha());

        // 4. Mapeia os dados do DTO para a Entidade
        Usuario usuario = new Usuario();
        usuario.setNome(dto.getNome());
        usuario.setEmail(emailFormatado);
        usuario.setSenhaHash(senhaCriptografada);
        usuario.setTelefone(dto.getTelefone()); // Campo novo trazido da branch do seu colega

        // 5. Salva e retorna o usuário
        return repository.save(usuario);
    }
}