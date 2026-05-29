package br.com.arreda.backend.services;
import br.com.arreda.backend.models.Usuario;
import br.com.arreda.backend.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import br.com.arreda.backend.dto.UsuarioCreateDTO;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final  UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Usuario salvarUsuario(UsuarioCreateDTO dto){

        String emailFormatado = dto.getEmail().toLowerCase().trim();

        if (repository.existsByEmail(emailFormatado)) {
            throw new IllegalArgumentException("Erro: O e-mail informado já está cadastrado.");
        }

        String senhaCriptografada = passwordEncoder.encode(dto.getSenha());

        Usuario usuario = new Usuario();
        usuario.setNome(dto.getNome());
        usuario.setEmail(emailFormatado);
        usuario.setSenhaHash(senhaCriptografada);
        usuario.setTelefone(dto.getTelefone());

        return repository.save(usuario);

    }
}