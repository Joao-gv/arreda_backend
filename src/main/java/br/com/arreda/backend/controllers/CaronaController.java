package br.com.arreda.backend.controllers;

import br.com.arreda.backend.dto.CaronaCreateDTO;
import br.com.arreda.backend.models.Usuario;
import br.com.arreda.backend.services.CaronaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import br.com.arreda.backend.dto.CaronaResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springdoc.core.annotations.ParameterObject;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/caronas")
@Tag(name = "Carona", description = "Todos os endpoints para criar e gerenciar uma carona")
@RequiredArgsConstructor
public class CaronaController {

    private final CaronaService caronaService;

    @PostMapping("/publicar")
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

    @PutMapping("/{idCarona}/participacoes/{idParticipacao}/aceitar")
    public ResponseEntity<Object> aceitarCarona(
            @PathVariable Long idCarona,
            @PathVariable Long idParticipacao
    ){
        Usuario usuarioLogado =
                (Usuario) SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getPrincipal();

        Long idLogado = usuarioLogado.getId();

        caronaService.aceitarPassageiro(idCarona, idParticipacao, idLogado);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{idCarona}/participacoes/{idParticipacao}/rejeitar")
    public ResponseEntity<Object> rejeitarCarona(
            @PathVariable Long idCarona,
            @PathVariable Long idParticipacao
    ) {

        Usuario usuarioLogado =
                (Usuario) SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getPrincipal();

        Long idLogado = usuarioLogado.getId();

        caronaService.rejeitarPassageiro(idCarona, idParticipacao, idLogado);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<CaronaResponseDTO>> listarCaronas(
            @RequestParam(required = false) String origem,
            @RequestParam(required = false) String destino,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @PageableDefault(size = 10, sort = "dataHoraPartida") @ParameterObject Pageable pageable
    ) {
        Page<CaronaResponseDTO> caronas = caronaService.buscarCaronas(origem, destino, data, pageable);
        return ResponseEntity.ok(caronas);
    }
}