package fr.maceun.api.config;

import java.nio.file.Path;
import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import fr.maceun.api.common.RateLimiter;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final MaceunProperties properties;

    public WebConfig(MaceunProperties properties) {
        this.properties = properties;
    }

    /** CORS utile en dev (Vite sur :5173). En prod, Nginx sert le front et l'API sur la même origine. */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(properties.cors().allowedOrigins().toArray(String[]::new))
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE")
                .allowCredentials(true);
    }

    /** Images envoyées depuis l'admin. Les noms sont uniques, donc cache long. */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        var location = Path.of(properties.uploads().dir()).toAbsolutePath().normalize().toUri().toString();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location)
                .setCacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic().immutable());
    }

    @Bean
    RestClient.Builder restClientBuilder() {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(10));
        return RestClient.builder().requestFactory(factory);
    }

    @Bean
    RateLimiter bookingRateLimiter() {
        var limit = properties.booking().rateLimit();
        return new RateLimiter(limit.maxRequests(), limit.window());
    }

    /** Anti brute-force sur la connexion admin : 10 essais par IP toutes les 15 min. */
    @Bean
    RateLimiter loginRateLimiter() {
        return new RateLimiter(10, Duration.ofMinutes(15));
    }
}
