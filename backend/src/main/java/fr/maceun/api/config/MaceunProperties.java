package fr.maceun.api.config;

import java.time.Duration;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "maceun")
public record MaceunProperties(Cors cors, Admin admin, Uploads uploads, Bandsintown bandsintown,
                               Releases releases, Booking booking) {

    public record Cors(List<String> allowedOrigins) {}

    public record Admin(String username, String password) {}

    public record Uploads(String dir) {}

    public record Bandsintown(String baseUrl, String artist, String appId, Duration cacheTtl) {}

    /**
     * @param allowedHosts domaines de liens acceptés (protège contre les requêtes vers le réseau interne)
     * @param deezerArtist nom exact de l'artiste sur Deezer, pour la recherche par titre
     * @param syncInterval fréquence de mise à jour automatique des infos
     * @param autoSync     mise à jour au démarrage puis périodique (désactivée dans les tests)
     */
    public record Releases(List<String> allowedHosts, String deezerArtist, Duration syncInterval, boolean autoSync) {}

    public record Booking(String formspreeUrl, RateLimit rateLimit) {}

    public record RateLimit(int maxRequests, Duration window) {}
}
