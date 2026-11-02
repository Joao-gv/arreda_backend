package br.com.arreda.backend.controllers;

import br.com.arreda.backend.dto.ConfirmarEmailDTO;
import br.com.arreda.backend.dto.LoginDTO;
import br.com.arreda.backend.dto.ReenviarConfirmacaoDTO;
import br.com.arreda.backend.dto.RefreshTokenRequestDTO;
import br.com.arreda.backend.dto.TokenResponseDTO;
import br.com.arreda.backend.dto.EsqueciSenhaDTO;
import br.com.arreda.backend.dto.RedefinirSenhaDTO;
import br.com.arreda.backend.models.RefreshToken;
import br.com.arreda.backend.models.Usuario;
import br.com.arreda.backend.repositories.UsuarioRepository;
import br.com.arreda.backend.security.RefreshTokenService;
import br.com.arreda.backend.security.TokenService;
import br.com.arreda.backend.services.ConfirmacaoEmailService;
import br.com.arreda.backend.services.RecuperacaoSenhaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Login", description = "Endpoints relacionados ao login")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    /** Código lido pelo frontend para mostrar a tela de "confirme seu e-mail" em vez de "senha inválida". */
    public static final String CODIGO_EMAIL_NAO_VERIFICADO = "EMAIL_NAO_VERIFICADO";

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private RecuperacaoSenhaService recuperacaoSenhaService;

    @Autowired
    private ConfirmacaoEmailService confirmacaoEmailService;

    @Operation(summary = "Login",
            description = "Retorna access e refresh token. 401 para credenciais inválidas; "
                    + "403 com codigo EMAIL_NAO_VERIFICADO quando a senha está correta mas o e-mail não foi confirmado.")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginDTO loginDTO) {
        Usuario usuario = usuarioRepository.findByEmail(loginDTO.email())
                .orElse(null);

        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("erro", "Email ou senha inválidos."));
        }


        if (!passwordEncoder.matches(loginDTO.senha(), usuario.getSenhaHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("erro", "Email ou senha inválidos"));
        }

        // Verificado só depois da senha, para não revelar o estado da conta a quem não sabe a senha.
        if (!usuario.isEmailVerificado()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "erro", "Confirme seu e-mail antes de entrar. Verifique sua caixa de entrada e o spam.",
                    "codigo", CODIGO_EMAIL_NAO_VERIFICADO,
                    "status", HttpStatus.FORBIDDEN.value()));
        }

        String accessToken = tokenService.gerarToken(usuario);

        RefreshToken refreshToken = refreshTokenService.criarRefreshToken(usuario.getId());

        return ResponseEntity.ok(new TokenResponseDTO(accessToken, refreshToken.getToken()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponseDTO> refresh(@RequestBody @Valid RefreshTokenRequestDTO request) {

        return refreshTokenService.buscarPorToken(request.refreshToken())
                .map(refreshTokenService::verificarExpiracao) // Verifica se não venceu
                .map(RefreshToken::getUsuario)                // Pega o dono do token
                .map(usuario -> {
                    // Se tudo estiver certo, gera novos tokens (Rotação total)
                    String novoAccessToken = tokenService.gerarToken(usuario);
                    RefreshToken novoRefreshToken = refreshTokenService.criarRefreshToken(usuario.getId());

                    return ResponseEntity.ok(new TokenResponseDTO(novoAccessToken, novoRefreshToken.getToken()));
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Refresh Token não encontrado ou inválido!"));
    }

    @PostMapping("/esqueci-senha")
    public ResponseEntity<?> esqueciSenha(@RequestBody @Valid EsqueciSenhaDTO dto) {
        if (!recuperacaoSenhaService.podeSolicitar(dto.email())) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("mensagem", "Muitas tentativas. Tente novamente mais tarde."));
        }
        recuperacaoSenhaService.solicitar(dto.email());
        // Resposta idêntica exista ou não a conta, para não revelar quem está cadastrado.
        return ResponseEntity.ok(Map.of("mensagem",
                "Se o e-mail estiver cadastrado, você receberá um link de redefinição."));
    }

    @PostMapping("/redefinir-senha")
    public ResponseEntity<?> redefinirSenha(@RequestBody @Valid RedefinirSenhaDTO dto) {
        recuperacaoSenhaService.redefinir(dto);
        return ResponseEntity.ok(Map.of("mensagem", "Senha redefinida com sucesso. Faça login novamente."));
    }

    @Operation(summary = "Confirmar e-mail",
            description = "Recebe o token do link enviado por e-mail e marca a conta como verificada. "
                    + "Token inválido, adulterado, expirado ou já usado retorna 400 com mensagem amigável.")
    @PostMapping("/confirmar-email")
    public ResponseEntity<?> confirmarEmail(@RequestBody @Valid ConfirmarEmailDTO dto) {
        confirmacaoEmailService.confirmar(dto.token());
        return ResponseEntity.ok(Map.of("mensagem", "E-mail confirmado com sucesso. Você já pode fazer login."));
    }

    @Operation(summary = "Reenviar e-mail de confirmação",
            description = "Sempre responde 200 com a mesma mensagem, exista ou não a conta. "
                    + "Limite de 1 reenvio por e-mail a cada 2 minutos (429 quando excedido).")
    @PostMapping("/reenviar-confirmacao")
    public ResponseEntity<?> reenviarConfirmacao(@RequestBody @Valid ReenviarConfirmacaoDTO dto) {
        if (!confirmacaoEmailService.podeReenviar(dto.email())) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("mensagem", "Aguarde 2 minutos antes de pedir um novo e-mail."));
        }
        confirmacaoEmailService.reenviar(dto.email());
        // Resposta idêntica exista ou não a conta (ou já verificada), para não revelar quem está cadastrado.
        return ResponseEntity.ok(Map.of("mensagem",
                "Se o e-mail estiver cadastrado e ainda não confirmado, você receberá um novo link de confirmação."));
    }
}
