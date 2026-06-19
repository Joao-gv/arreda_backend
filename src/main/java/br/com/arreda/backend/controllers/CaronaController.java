package br.com.arreda.backend.controllers;

import br.com.arreda.backend.dto.CaronaCreateDTO;
import br.com.arreda.backend.models.Usuario;
import br.com.arreda.backend.services.CaronaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/caronas")
@RequiredArgsConstructor
public class CaronaController {

    private final CaronaService caronaService;

    @PostMapping
    public ResponseEntity<Object> publicarCarona(
            @RequestBody @Valid CaronaCreateDTO dto) {

        Usuario usuarioLogado =
                (Usuario) SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getPrincipal();

        var caronaSalva =
                caronaService.publicarCarona(dto, usuarioLogado);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(caronaSalva);
    }

    @PostMapping("/{id}/solicitar")
    public ResponseEntity<Object> solicitarParticipacao(
            @PathVariable Long id) {

        Usuario usuarioLogado =
                (Usuario) SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getPrincipal();

        var solicitacao =
                caronaService.solicitarParticipacao(id, usuarioLogado);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(solicitacao);
    }
}