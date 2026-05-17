package br.com.arreda.backend.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/teste")
@Tag(name = "Rota de Teste", description = "Endpoint para verificar se o sistema está online")
public class TesteController {

    @GetMapping("/hello")
    @Operation(summary = "Retorna uma mensagem de boas-vindas", description = "Serve para testar a conexão com o backend")
    public String dizerOla() {
        return "🚗 Arreda Backend está rodando com sucesso!";
    }
}