package br.com.arreda.backend.services;

import br.com.arreda.backend.exception.RecursoNaoEncontradoException;
import br.com.arreda.backend.exception.RegraDeNegocioException;
import br.com.arreda.backend.models.Usuario;
import br.com.arreda.backend.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

@Slf4j
@Service
@RequiredArgsConstructor
public class FotoPerfilService {
    public static final long TAMANHO_MAXIMO_BYTES = 2L * 1024 * 1024;
    private static final long PIXELS_MAXIMOS = 20_000_000L;

    private final UsuarioRepository usuarioRepository;
    private final ArmazenamentoFotoPerfil armazenamento;

    @Transactional
    public String atualizarFoto(Long usuarioId, MultipartFile arquivo) {
        Usuario usuario = buscarUsuario(usuarioId);
        byte[] jpeg = converterParaJpegRedimensionado(arquivo);
        String chaveNova;
        try {
            chaveNova = armazenamento.salvar(jpeg);
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível salvar a foto de perfil.", e);
        }

        String chaveAntiga = usuario.getFotoKey();
        usuario.setFotoKey(chaveNova);
        try {
            usuarioRepository.save(usuario);
        } catch (RuntimeException e) {
            excluirSilenciosamente(chaveNova);
            throw e;
        }
        limparArquivosAposCommit(chaveNova, chaveAntiga);
        return chaveNova;
    }

    @Transactional
    public void removerFoto(Long usuarioId) {
        Usuario usuario = buscarUsuario(usuarioId);
        String chaveAntiga = usuario.getFotoKey();
        if (chaveAntiga == null) {
            return;
        }

        usuario.setFotoKey(null);
        usuarioRepository.save(usuario);
        limparArquivosAposCommit(null, chaveAntiga);
    }

    @Transactional(readOnly = true)
    public byte[] buscarFoto(Long usuarioId) {
        Usuario usuario = buscarUsuario(usuarioId);
        if (usuario.getFotoKey() == null) {
            throw new RecursoNaoEncontradoException("Este usuário ainda não possui foto de perfil.");
        }

        try {
            byte[] conteudo = armazenamento.ler(usuario.getFotoKey());
            if (conteudo == null) {
                throw new RecursoNaoEncontradoException("Foto de perfil não encontrada.");
            }
            return conteudo;
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível ler a foto de perfil.", e);
        }
    }

    private Usuario buscarUsuario(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));
    }

    private byte[] converterParaJpegRedimensionado(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new RegraDeNegocioException("Envie uma imagem JPEG ou PNG.");
        }
        if (arquivo.getSize() > TAMANHO_MAXIMO_BYTES) {
            throw new RegraDeNegocioException("A imagem deve ter no máximo 2 MB.");
        }

        try {
            byte[] bytes = arquivo.getBytes();
            String formatoAssinatura = identificarAssinatura(bytes);
            if (formatoAssinatura == null) {
                throw new RegraDeNegocioException("A imagem deve ser JPEG ou PNG válida.");
            }

            BufferedImage imagem = decodificarImagem(bytes, formatoAssinatura);
            double escala = Math.min(1.0, Math.min(512.0 / imagem.getWidth(), 512.0 / imagem.getHeight()));
            ByteArrayOutputStream saida = new ByteArrayOutputStream();
            Thumbnails.of(imagem)
                    .scale(escala)
                    .outputFormat("jpg")
                    .outputQuality(0.85)
                    .toOutputStream(saida);
            return saida.toByteArray();
        } catch (RegraDeNegocioException e) {
            throw e;
        } catch (IOException | RuntimeException e) {
            throw new RegraDeNegocioException("Não foi possível processar a imagem enviada.");
        }
    }

    private String identificarAssinatura(byte[] bytes) {
        boolean jpeg = bytes.length >= 3
                && (bytes[0] & 0xFF) == 0xFF
                && (bytes[1] & 0xFF) == 0xD8
                && (bytes[2] & 0xFF) == 0xFF;
        boolean png = bytes.length >= 8
                && (bytes[0] & 0xFF) == 0x89
                && bytes[1] == 0x50
                && bytes[2] == 0x4E
                && bytes[3] == 0x47
                && bytes[4] == 0x0D
                && bytes[5] == 0x0A
                && bytes[6] == 0x1A
                && bytes[7] == 0x0A;
        return jpeg ? "JPEG" : png ? "PNG" : null;
    }

    private BufferedImage decodificarImagem(byte[] bytes, String formatoEsperado) throws IOException {
        try (ImageInputStream entrada = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            if (entrada == null) {
                throw new RegraDeNegocioException("A imagem enviada é inválida.");
            }

            Iterator<ImageReader> leitores = ImageIO.getImageReaders(entrada);
            if (!leitores.hasNext()) {
                throw new RegraDeNegocioException("A imagem enviada é inválida.");
            }

            ImageReader leitor = leitores.next();
            try {
                leitor.setInput(entrada, true, true);
                if (!formatoEsperado.equalsIgnoreCase(leitor.getFormatName())) {
                    throw new RegraDeNegocioException("A imagem deve ser JPEG ou PNG válida.");
                }
                int largura = leitor.getWidth(0);
                int altura = leitor.getHeight(0);
                if (largura <= 0 || altura <= 0 || (long) largura * altura > PIXELS_MAXIMOS) {
                    throw new RegraDeNegocioException("A resolução da imagem excede o limite permitido.");
                }
                BufferedImage imagem = leitor.read(0);
                if (imagem == null) {
                    throw new RegraDeNegocioException("A imagem enviada é inválida.");
                }
                return imagem;
            } finally {
                leitor.dispose();
            }
        }
    }

    private void limparArquivosAposCommit(String chaveNova, String chaveAntiga) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_COMMITTED) {
                    excluirSilenciosamente(chaveAntiga);
                } else {
                    excluirSilenciosamente(chaveNova);
                }
            }
        });
    }

    private void excluirSilenciosamente(String chave) {
        if (chave == null) {
            return;
        }
        try {
            armazenamento.excluir(chave);
        } catch (IOException | RuntimeException e) {
            log.warn("Não foi possível limpar o arquivo de foto {}.", chave, e);
        }
    }
}
