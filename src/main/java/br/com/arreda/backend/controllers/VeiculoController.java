package br.com.arreda.backend.controllers;

import br.com.arreda.backend.dto.VeiculoCreateDTO;
import br.com.arreda.backend.models.Usuario;
import br.com.arreda.backend.services.VeiculoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/motorista/veiculos")
@Tag(name = "Veículos", description = "Todos os endpoints para adicionar e gerenciar veículos")
@RequiredArgsConstructor
public class VeiculoController {

    private final VeiculoService veiculoService;

    @PostMapping
    public ResponseEntity<?> cadastrarVeiculo(@Valid @RequestBody VeiculoCreateDTO dto) {
        try {
            // Objects.requireNonNull garante que a autenticação existe no contexto
            var authentication = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication(), "Autenticação não encontrada.");
            Usuario usuarioLogado = (Usuario) authentication.getPrincipal();

            String emailAutenticado = usuarioLogado.getEmail();

            veiculoService.cadastrarVeiculo(dto, emailAutenticado);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("mensagem", "Veículo cadastrado com sucesso!"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("erro", e.getMessage()));
        }
    }
}
