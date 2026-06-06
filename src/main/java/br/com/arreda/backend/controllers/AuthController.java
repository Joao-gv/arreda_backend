package br.com.arreda.backend.controllers;

import br.com.arreda.backend.dto.LoginDTO;
import br.com.arreda.backend.dto.RefreshTokenRequestDTO;
import br.com.arreda.backend.dto.TokenResponseDTO;
import br.com.arreda.backend.models.RefreshToken;
import br.com.arreda.backend.models.Usuario;
import br.com.arreda.backend.repositories.UsuarioRepository;
import br.com.arreda.backend.security.RefreshTokenService;
import br.com.arreda.backend.security.TokenService;
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
public class AuthController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private RefreshTokenService refreshTokenService;

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
}