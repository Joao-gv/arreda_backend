package br.com.arreda.backend.services;

import br.com.arreda.backend.dto.UsuarioUpdateDTO;
import br.com.arreda.backend.exception.RecursoNaoEncontradoException;
import br.com.arreda.backend.models.Usuario;
import br.com.arreda.backend.repositories.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService service;

    private Usuario criarUsuario() {
        Usuario u = new Usuario();
        u.setId(1L);
        u.setNome("Nome Antigo");
        u.setEmail("antigo@email.com");
        u.setSenhaHash("hash-original");
        u.setTelefone("31900000000");
        return u;
    }

    @Test
    void atualizarPerfil_deveAlterarApenasNomeETelefone() {
        Usuario usuario = criarUsuario();
        when(repository.findById(1L)).thenReturn(Optional.of(usuario));
        when(repository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario resultado = service.atualizarPerfil(1L,
                new UsuarioUpdateDTO("  Nome Novo  ", "31988887777"));

        assertEquals("Nome Novo", resultado.getNome());
        assertEquals("31988887777", resultado.getTelefone());
        // campos protegidos continuam intactos
        assertEquals("antigo@email.com", resultado.getEmail());
        assertEquals("hash-original", resultado.getSenhaHash());
    }

    @Test
    void atualizarPerfil_usuarioInexistente_deveLancarErro() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class,
                () -> service.atualizarPerfil(99L, new UsuarioUpdateDTO("Nome", "31988887777")));

        verify(repository, never()).save(any());
    }
}