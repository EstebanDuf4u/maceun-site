package fr.maceun.api.config;

import java.time.Duration;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "maceun")
public record MaceunProperties(Cors cors, Admin admin, Uploads uploads, Bandsintown bandsintown, Booking booking) {

    public record Cors(List<String> allowedOrigins) {}

    public record Admin(String username, String password) {}

    public record Uploads(String dir) {}

    public record Bandsintown(String baseUrl, String artist, String appId, Duration cacheTtl) {}

    public record Booking(String formspreeUrl, RateLimit rateLimit) {}

    public record RateLimit(int maxRequests, Duration window) {}
}
