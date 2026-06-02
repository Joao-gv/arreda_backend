package br.com.arreda.backend.controllers;

import br.com.arreda.backend.dto.LoginDTO;
import br.com.arreda.backend.models.Usuario;
import br.com.arreda.backend.repositories.UsuarioRepository;
import br.com.arreda.backend.security.TokenService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenService tokenService;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody @Valid LoginDTO loginDTO) {
        // 1. Busca o usuário pelo e-mail
        Usuario usuario = usuarioRepository.findByEmail(loginDTO.email())
                .orElseThrow(() -> new RuntimeException("Credenciais inválidas"));
        // 2. Compara a senha digitada (texto puro) com o Hash do banco
        if (!passwordEncoder.matches(loginDTO.senha(), usuario.getSenhaHash())) {
            // Se a senha estiver errada, retorna 401 Unauthorized
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciais inválidas");
        }

        // 3. Se tudo estiver correto, gera o token JWT
        String token = tokenService.gerarToken(usuario);

        // 4. Retorna o token no corpo da resposta
        return ResponseEntity.ok(token);
    }
}