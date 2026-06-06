package br.com.arreda.backend.controllers;


import br.com.arreda.backend.dto.PerfilMotoristaCreateDTO;
import br.com.arreda.backend.services.PerfilMotoristaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import br.com.arreda.backend.models.Usuario;

@RestController
@RequestMapping("/api/motorista/perfil")
@RequiredArgsConstructor
public class PerfilMotoristaController {
    private final PerfilMotoristaService perfilMotoristaService;

    @PostMapping
    public ResponseEntity<?> criarPerfil(@Valid @RequestBody PerfilMotoristaCreateDTO dto){
        try{
        //pega o email do usuário autenticado
        var principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        //transforma o usuario em email
        br.com.arreda.backend.models.Usuario usuarioLogado = (br.com.arreda.backend.models.Usuario) principal;
        String emailAutenticado = usuarioLogado.getEmail();
        //passo dto e email para o service
        perfilMotoristaService.criarPerfil(dto, emailAutenticado);

        return ResponseEntity.status(HttpStatus.CREATED).body("Perfil de motorista criado com sucesso");
    } catch (IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
