package br.com.arreda.backend.controllers;

import br.com.arreda.backend.dto.UsuarioCreateDTO;
import br.com.arreda.backend.services.UsuarioService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/usuarios")
@Tag(name = "Cadastro", description = "Cadastrar novos usuários")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class UsuarioController {
   private final UsuarioService usuarioService;
   @PostMapping("register")
    public ResponseEntity<?> registrarUsuario(@Valid @RequestBody UsuarioCreateDTO dto){
       try{
           usuarioService.salvarUsuario(dto);

           return ResponseEntity.status(HttpStatus.CREATED)
                   .body("Usuário cadastrado com sucesso!");
       } catch (IllegalArgumentException e){
           return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                   .body(e.getMessage());
       }
   }
}
