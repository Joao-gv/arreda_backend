package br.com.arreda.backend.services;

import br.com.arreda.backend.enums.TipoToken;
import br.com.arreda.backend.exception.RegraDeNegocioException;
import br.com.arreda.backend.models.TokenVerificacao;
import br.com.arreda.backend.models.Usuario;
import br.com.arreda.backend.repositories.TokenVerificacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenVerificacaoService {

    private final TokenVerificacaoRepository repository;

    @Transactional
    public TokenVerificacao gerar(Usuario usuario, TipoToken tipo){
        TokenVerificacao t = new TokenVerificacao();
        t.setToken(UUID.randomUUID().toString());
        t.setUsuario(usuario);
        t.setTipo(tipo);
        t.setExpiraEm(LocalDateTime.now().plus(validadePara(tipo)));
        return repository.save(t);
    }

    @Transactional
    public Usuario validarEConsumir(String token,TipoToken tipo){
     TokenVerificacao t = repository.findByTokenAndTipo(token, tipo)
        .orElseThrow(() -> new RegraDeNegocioException("Link invalido"));

        if(t.getUsadoEm() != null) {
            throw new RegraDeNegocioException("Este link já foi utilizado.");
        }
        if (LocalDateTime.now().isAfter(t.getExpiraEm())){
            throw new RegraDeNegocioException("Este link expirou. Solicite um novo.");
        }
        LocalDateTime agora = LocalDateTime.now();
        t.setUsadoEm(agora);
        // Usar um link invalida os demais links pendentes do mesmo tipo (ex.: pedidos de reset anteriores).
        repository.invalidarPendentes(t.getUsuario(), tipo, agora);
        return t.getUsuario();
    }
    private Duration validadePara(TipoToken tipo){
        return switch (tipo){
            case CONFIRMACAO_EMAIL -> Duration.ofHours(24);
            case RESET_SENHA -> Duration.ofMinutes(30);
        };
    }
}
