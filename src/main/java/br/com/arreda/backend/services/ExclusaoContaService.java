package br.com.arreda.backend.services;

import br.com.arreda.backend.enums.StatusCarona;
import br.com.arreda.backend.enums.StatusParticipacao;
import br.com.arreda.backend.enums.TipoToken;
import br.com.arreda.backend.exception.RecursoNaoEncontradoException;
import br.com.arreda.backend.exception.RegraDeNegocioException;
import br.com.arreda.backend.models.TokenVerificacao;
import br.com.arreda.backend.models.Usuario;
import br.com.arreda.backend.repositories.CaronaRepository;
import br.com.arreda.backend.repositories.ParticipacaoCaronaRepository;
import br.com.arreda.backend.repositories.PerfilMotoristaRepository;
import br.com.arreda.backend.repositories.RefreshTokenRepository;
import br.com.arreda.backend.repositories.TokenVerificacaoRepository;
import br.com.arreda.backend.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExclusaoContaService {
    private static final List<StatusCarona> CARONAS_EM_ANDAMENTO = List.of(StatusCarona.ATIVA, StatusCarona.LOTADA);

    private final UsuarioRepository usuarioRepository;
    private final ParticipacaoCaronaRepository participacaoRepository;
    private final CaronaRepository caronaRepository;
    private final PerfilMotoristaRepository perfilMotoristaRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenVerificacaoRepository tokenVerificacaoRepository;
    private final FotoPerfilService fotoPerfilService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void excluirConta(Long usuarioId, String senha) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .filter(Usuario::isAtivo)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario não encontrado"));

        if (!passwordEncoder.matches(senha, usuario.getSenhaHash())) {
            throw new RegraDeNegocioException("Senha incorreta.");
        }

        boolean participaDeCaronaEmAndamento = participacaoRepository
                .existsByUsuarioIdAndStatusAndCaronaStatusIn(usuarioId, StatusParticipacao.CONFIRMADO, CARONAS_EM_ANDAMENTO);
        boolean dirigeCaronaEmAndamento = caronaRepository
                .existsByPerfilMotoristaUsuarioIdAndStatusIn(usuarioId, CARONAS_EM_ANDAMENTO);
        if (participaDeCaronaEmAndamento || dirigeCaronaEmAndamento) {
            throw new RegraDeNegocioException("Você tem uma carona em andamento. Conclua ou cancele antes de excluir a conta.");
        }
        participacaoRepository.atualizarStatusDoUsuario(
                usuarioId, StatusParticipacao.PENDENTE, StatusParticipacao.CANCELADO);

        refreshTokenRepository.deleteByUsuarioId(usuarioId);

        perfilMotoristaRepository.findByUsuarioId(usuarioId).ifPresent(perfil -> {
            perfil.setCnh("Removido-" + perfil.getId());
            perfil.getVeiculos().forEach(veiculo -> veiculo.setPlaca("Removido-" + veiculo.getId()));
            perfilMotoristaRepository.save(perfil);
        });
        fotoPerfilService.removerFoto(usuarioId);
        usuario.setNome("Usuário removido");
        usuario.setEmail("removido-" + usuario.getId() + "@arreda.invalid");
        usuario.setTelefone("0000000000");
        usuario.setSenhaHash(passwordEncoder.encode(UUID.randomUUID().toString()));
        usuario.setFotoKey(null);
        usuario.setAtivo(false);
        usuario.setAnonimizadoEm(LocalDateTime.now());
        usuarioRepository.save(usuario);


        LocalDateTime agora = LocalDateTime.now();
        for (TipoToken tipo : TipoToken.values()) {
            tokenVerificacaoRepository.invalidarPendentes(usuario, tipo, agora);
        }
    }
}
