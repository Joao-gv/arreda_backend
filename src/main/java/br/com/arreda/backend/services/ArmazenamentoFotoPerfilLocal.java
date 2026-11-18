package br.com.arreda.backend.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.regex.Pattern;

@Component
public class ArmazenamentoFotoPerfilLocal implements ArmazenamentoFotoPerfil {
    private static final Pattern CHAVE_FOTO = Pattern.compile("foto/[0-9a-fA-F-]{36}\\.jpg");

    private final Path diretorio;

    public ArmazenamentoFotoPerfilLocal(
            @Value("${arreda.storage.fotos-dir:./uploads}") String diretorio) {
        this.diretorio = Path.of(diretorio).toAbsolutePath().normalize();
    }

    @Override
    public String salvar(byte[] conteudo) throws IOException {
        Path pastaFotos = diretorio.resolve("foto");
        Files.createDirectories(pastaFotos);

        String chave = "foto/" + UUID.randomUUID() + ".jpg";
        Path destino = resolver(chave);
        Path temporario = Files.createTempFile(pastaFotos, ".foto-", ".tmp");
        try {
            Files.write(temporario, conteudo);
            try {
                Files.move(temporario, destino, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporario, destino);
            }
            return chave;
        } finally {
            Files.deleteIfExists(temporario);
        }
    }

    @Override
    public byte[] ler(String chave) throws IOException {
        Path arquivo = resolver(chave);
        return Files.exists(arquivo) ? Files.readAllBytes(arquivo) : null;
    }

    @Override
    public void excluir(String chave) throws IOException {
        Files.deleteIfExists(resolver(chave));
    }

    private Path resolver(String chave) {
        if (chave == null || !CHAVE_FOTO.matcher(chave).matches()) {
            throw new IllegalArgumentException("Chave de foto inválida.");
        }

        Path arquivo = diretorio.resolve(chave).normalize();
        if (!arquivo.startsWith(diretorio)) {
            throw new IllegalArgumentException("Chave de foto inválida.");
        }
        return arquivo;
    }
}
