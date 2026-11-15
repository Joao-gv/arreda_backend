package br.com.arreda.backend.controllers;

import br.com.arreda.backend.dto.HistoricoCaronaDTO;
import br.com.arreda.backend.dto.FotoUploadDTO;
import br.com.arreda.backend.dto.UsuarioCreateDTO;
import br.com.arreda.backend.dto.UsuarioMeDTO;
import br.com.arreda.backend.dto.VeiculoResponseDTO;
import br.com.arreda.backend.models.Carona;
import br.com.arreda.backend.models.Usuario;
import br.com.arreda.backend.services.CaronaService;
import br.com.arreda.backend.services.UsuarioService;
import br.com.arreda.backend.services.VeiculoService;
import br.com.arreda.backend.services.FotoPerfilService;
import br.com.arreda.backend.repositories.PerfilMotoristaRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.http.CacheControl;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import br.com.arreda.backend.dto.UsuarioUpdateDTO;

import java.util.ArrayList;
import java.util.List;
import java.time.Duration;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("api/usuarios")
@Tag(name = "Usuário", description = "Todos os endpoints relacionados ao usuário")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class UsuarioController {
   private final UsuarioService usuarioService;
   private final CaronaService caronaService;
   private final VeiculoService veiculoService;
   private final PerfilMotoristaRepository perfilMotoristaRepository;
   private final FotoPerfilService fotoPerfilService;

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

    @GetMapping("/me")
    public ResponseEntity<UsuarioMeDTO> obterUsuarioLogado(
            @AuthenticationPrincipal Usuario usuarioLogado) {
        return ResponseEntity.ok(montarUsuarioMeDTO(usuarioLogado));
    }

    @PutMapping("/me")
    public ResponseEntity<UsuarioMeDTO> atualizarUsuarioLogado(
            @AuthenticationPrincipal Usuario usuarioLogado,
            @Valid @RequestBody UsuarioUpdateDTO dto) {
        // O id vem do token (principal), nunca do corpo da requisição
        Usuario atualizado = usuarioService.atualizarPerfil(usuarioLogado.getId(), dto);
        return ResponseEntity.ok(montarUsuarioMeDTO(atualizado));
    }

    @PostMapping(value = "/me/foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FotoUploadDTO> enviarFotoPerfil(
            @AuthenticationPrincipal Usuario usuarioLogado,
            @RequestParam("foto") MultipartFile foto) {
        String fotoKey = fotoPerfilService.atualizarFoto(usuarioLogado.getId(), foto);
        return ResponseEntity.ok(new FotoUploadDTO(urlFoto(usuarioLogado.getId(), fotoKey)));
    }

    @DeleteMapping("/me/foto")
    public ResponseEntity<Void> removerFotoPerfil(@AuthenticationPrincipal Usuario usuarioLogado) {
        fotoPerfilService.removerFoto(usuarioLogado.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/foto")
    public ResponseEntity<byte[]> obterFotoPerfil(@PathVariable Long id) {
        byte[] conteudo = fotoPerfilService.buscarFoto(id);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .cacheControl(CacheControl.maxAge(Duration.ofHours(1)).cachePublic())
                .body(conteudo);
    }

    private UsuarioMeDTO montarUsuarioMeDTO(Usuario usuario) {
        boolean ehMotorista = perfilMotoristaRepository.findByUsuarioId(usuario.getId()).isPresent();
        return new UsuarioMeDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getTelefone(),
                ehMotorista,
                urlFoto(usuario.getId(), usuario.getFotoKey())
        );
    }

    private String urlFoto(Long usuarioId, String fotoKey) {
        if (fotoKey == null) {
            return null;
        }
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/usuarios/{id}/foto")
                .queryParam("v", fotoKey.substring("foto/".length(), fotoKey.length() - ".jpg".length()))
                .buildAndExpand(usuarioId)
                .toUriString();
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
