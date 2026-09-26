package fr.maceun.api.storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import fr.maceun.api.config.MaceunProperties;

/**
 * Stocke les images envoyées depuis l'admin dans un dossier (volume Docker)
 * et les expose sous /uploads/.
 */
@Service
public class StorageService {

    public static final String PUBLIC_PREFIX = "/uploads/";
    private static final Logger log = LoggerFactory.getLogger(StorageService.class);

    private final Path root;

    public StorageService(MaceunProperties properties) throws IOException {
        this.root = Path.of(properties.uploads().dir()).toAbsolutePath().normalize();
        Files.createDirectories(root);
    }

    public record StoredImage(String src, Integer width, Integer height) {}

    public StoredImage store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidImageException("Aucun fichier reçu.");
        }
        try {
            byte[] bytes = file.getBytes();
            // on se fie au contenu réel du fichier, pas à son nom ni au type annoncé par le navigateur
            String ext = detectExtension(bytes);
            String name = UUID.randomUUID() + "." + ext;
            Files.write(root.resolve(name), bytes);
            var dims = readDimensions(bytes);
            return new StoredImage(PUBLIC_PREFIX + name, dims[0], dims[1]);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Supprime un fichier envoyé via l'admin. Les images d'origine (/img/...) ne sont jamais touchées. */
    public void deleteIfUploaded(String src) {
        if (src == null || !src.startsWith(PUBLIC_PREFIX)) return;
        var file = root.resolve(src.substring(PUBLIC_PREFIX.length())).normalize();
        if (!file.startsWith(root)) return;
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            log.warn("Impossible de supprimer {} : {}", file, e.getMessage());
        }
    }

    private static String detectExtension(byte[] b) {
        if (b.length > 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) return "jpg";
        if (b.length > 8 && Arrays.equals(Arrays.copyOf(b, 8),
                new byte[] {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'})) return "png";
        if (b.length > 12 && new String(b, 0, 4).equals("RIFF") && new String(b, 8, 4).equals("WEBP")) return "webp";
        throw new InvalidImageException("Format non supporté : utilisez JPG, PNG ou WebP.");
    }

    /** Largeur/hauteur si Java sait lire le format (JPG, PNG), sinon null. */
    private static Integer[] readDimensions(byte[] bytes) {
        try (InputStream in = new java.io.ByteArrayInputStream(bytes)) {
            var img = ImageIO.read(in);
            if (img != null) return new Integer[] {img.getWidth(), img.getHeight()};
        } catch (IOException ignored) {
            // format illisible par ImageIO (WebP) : pas grave
        }
        return new Integer[] {null, null};
    }
}
