package br.com.arreda.backend.services;
import br.com.arreda.backend.models.Usuario;
import br.com.arreda.backend.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final  UsuarioRepository repository;

    public boolean salvarUsuario(@NonNull Usuario usuario){

        if (usuario.getNome() == null || usuario.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("Erro: O nome do usuário é obrigatório.");
        }

        if (usuario.getSenhaHash() == null || usuario.getSenhaHash().trim().isEmpty()) {
            throw new IllegalArgumentException("Erro: A senha é obrigatória.");
        }

        if (usuario.getSenhaHash().length() < 6) {
            throw new IllegalArgumentException("Erro: A senha deve conter no mínimo 6 caracteres.");
        }

        boolean existingUserEmail = repository.existsByEmail(usuario.getEmail());

        if (existingUserEmail) {
            throw new IllegalArgumentException("Erro: O e-mail informado já está cadastrado.");
        }

        repository.save(usuario);
        return true;

    }
}