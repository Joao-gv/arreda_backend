package br.com.arreda.backend.controllers;

import br.com.arreda.backend.dto.HistoricoCaronaDTO;
import br.com.arreda.backend.dto.UsuarioCreateDTO;
import br.com.arreda.backend.dto.VeiculoResponseDTO;
import br.com.arreda.backend.models.Carona;
import br.com.arreda.backend.models.Usuario;
import br.com.arreda.backend.services.CaronaService;
import br.com.arreda.backend.services.UsuarioService;
import br.com.arreda.backend.services.VeiculoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("api/usuarios")
@Tag(name = "Usuário", description = "Todos os endpoints relacionados ao usuário")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class UsuarioController {
   private final UsuarioService usuarioService;

    private final CaronaService caronaService;
    private final VeiculoService veiculoService;


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

    @GetMapping("/me/caronas")
    public ResponseEntity<HistoricoCaronaDTO> obterHistoricoCaronas() {

        Usuario usuarioLogado =
                (Usuario) SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getPrincipal();

        Long idLogado = usuarioLogado.getId();

        HistoricoCaronaDTO historico = caronaService.buscarHistoricoUsuario(idLogado);
        return ResponseEntity.ok(historico);
    }

    @GetMapping("/me/veiculos")
    public ResponseEntity<List<VeiculoResponseDTO>> listarVeiculos() {

        Usuario usuarioLogado =
                (Usuario) SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getPrincipal();

        Long idLogado = usuarioLogado.getId();

       List<VeiculoResponseDTO> veiculosDTO = veiculoService.listarVeiculosDoUsuario(idLogado);
        return ResponseEntity.ok(veiculosDTO);
    }
}
