package br.com.arreda.backend.services;

import br.com.arreda.backend.enums.StatusParticipacao;
import br.com.arreda.backend.exception.RegraDeNegocioException;
import br.com.arreda.backend.models.Usuario;
import br.com.arreda.backend.repositories.CaronaRepository;
import br.com.arreda.backend.repositories.ParticipacaoCaronaRepository;
import br.com.arreda.backend.repositories.PerfilMotoristaRepository;
import br.com.arreda.backend.repositories.RefreshTokenRepository;
import br.com.arreda.backend.repositories.TokenVerificacaoRepository;
import br.com.arreda.backend.repositories.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExclusaoContaServiceTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private ParticipacaoCaronaRepository participacaoRepository;
    @Mock private CaronaRepository caronaRepository;
    @Mock private PerfilMotoristaRepository perfilMotoristaRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private TokenVerificacaoRepository tokenVerificacaoRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private FotoPerfilService fotoPerfilService;

    @InjectMocks private ExclusaoContaService service;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Maria Silva");
        usuario.setEmail("maria@aluno.ifmg.edu.br");
        usuario.setTelefone("31999998888");
        usuario.setSenhaHash("hash-original");
        usuario.setFotoKey("foto/abc123.jpg");
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
    }

    @Test
    void senhaIncorreta_naoAlteraNada() {
        // 1. Preparar
        when(passwordEncoder.matches("errada", "hash-original")).thenReturn(false);

        // 2. Executar (e esperar a exceção)
        assertThrows(RegraDeNegocioException.class, () -> service.excluirConta(1L, "errada"));

        // 3. Verificar: nada foi alterado
        assertEquals("Maria Silva", usuario.getNome());
        assertEquals("foto/abc123.jpg", usuario.getFotoKey());
        verify(usuarioRepository, never()).save(any());
        verify(refreshTokenRepository, never()).deleteByUsuarioId(any());
        verify(fotoPerfilService, never()).removerFoto(any());
    }

    @Test
    void caronaEmAndamento_bloqueiaExclusao() {
        // 1. Preparar
        when(passwordEncoder.matches("certa", "hash-original")).thenReturn(true);
        when(participacaoRepository.existsByUsuarioIdAndStatusAndCaronaStatusIn(
                eq(1L), eq(StatusParticipacao.CONFIRMADO), any())).thenReturn(true);
        when(caronaRepository.existsByPerfilMotoristaUsuarioIdAndStatusIn(eq(1L), any())).thenReturn(false);

        // 2. Executar (e esperar a exceção)
        assertThrows(RegraDeNegocioException.class, () -> service.excluirConta(1L, "certa"));

        // 3. Verificar: nada foi alterado
        assertEquals("Maria Silva", usuario.getNome());
        verify(usuarioRepository, never()).save(any());
        verify(fotoPerfilService, never()).removerFoto(any());
    }

    @Test
    void exclusaoValida_anonimizaEEncerraSessoes() {
        // 1. Preparar
        when(passwordEncoder.matches("certa", "hash-original")).thenReturn(true);
        when(participacaoRepository.existsByUsuarioIdAndStatusAndCaronaStatusIn(eq(1L), any(), any())).thenReturn(false);
        when(caronaRepository.existsByPerfilMotoristaUsuarioIdAndStatusIn(eq(1L), any())).thenReturn(false);
        when(perfilMotoristaRepository.findByUsuarioId(1L)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hash-aleatorio");

        // 2. Executar
        service.excluirConta(1L, "certa");

        // 3. Verificar: dados pessoais anonimizados
        assertEquals("Usuário removido", usuario.getNome());
        assertEquals("removido-1@arreda.invalid", usuario.getEmail());
        assertEquals("0000000000", usuario.getTelefone());
        assertEquals("hash-aleatorio", usuario.getSenhaHash());
        assertNull(usuario.getFotoKey());
        assertFalse(usuario.isAtivo());
        assertNotNull(usuario.getAnonimizadoEm());

        // 3. Verificar: sessões, pedidos pendentes e foto
        verify(fotoPerfilService).removerFoto(1L);
        verify(refreshTokenRepository).deleteByUsuarioId(1L);
        verify(participacaoRepository).atualizarStatusDoUsuario(
                1L, StatusParticipacao.PENDENTE, StatusParticipacao.CANCELADO);
        verify(usuarioRepository).save(usuario);
    }
}