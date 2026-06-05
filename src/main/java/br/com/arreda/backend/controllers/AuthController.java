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

import java.util.Map;

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

        //Se tudo estiver correto, gera o token JWT
        String token = tokenService.gerarToken(usuario);

        //Retorna o token estruturado dentro de um objeto JSON
        return ResponseEntity.ok(Map.of("token", token));
    }
}