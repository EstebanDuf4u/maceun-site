package fr.maceun.api.event;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import fr.maceun.api.config.MaceunProperties;

/**
 * Récupère les prochaines dates sur Bandsintown côté serveur (pas de souci CORS, clé non exposée)
 * et les garde en cache quelques minutes.
 */
@Service
public class EventService {

    private static final Logger log = LoggerFactory.getLogger(EventService.class);
    private static final int MAX_EVENTS = 12;

    private final RestClient client;
    private final MaceunProperties.Bandsintown config;

    private volatile List<Event> cached = List.of();
    private volatile Instant cachedAt = Instant.EPOCH;

    public EventService(RestClient.Builder builder, MaceunProperties properties) {
        this.config = properties.bandsintown();
        this.client = builder.baseUrl(config.baseUrl()).build();
    }

    public synchronized List<Event> getUpcomingEvents() {
        if (Instant.now().isBefore(cachedAt.plus(config.cacheTtl()))) {
            return cached;
        }
        try {
            var raw = client.get()
                    .uri(uri -> uri.path("/artists/{artist}/events")
                            .queryParam("app_id", config.appId())
                            .queryParam("date", "upcoming")
                            .build(config.artist()))
                    .retrieve()
                    .body(BandsintownEvent[].class);
            cached = raw == null ? List.of() : Arrays.stream(raw)
                    .filter(Objects::nonNull)
                    .map(EventService::toEvent)
                    .limit(MAX_EVENTS)
                    .toList();
            cachedAt = Instant.now();
        } catch (RestClientException e) {
            // on garde l'ancien cache plutôt que de renvoyer une erreur au visiteur
            log.warn("Bandsintown indisponible : {}", e.getMessage());
        }
        return cached;
    }

    private static Event toEvent(BandsintownEvent e) {
        var venue = e.venue();
        return new Event(
                e.id(),
                e.datetime() == null ? null : LocalDateTime.parse(e.datetime()),
                venue == null ? null : venue.name(),
                venue == null ? null : venue.city(),
                venue == null ? null : venue.country(),
                e.url());
    }
}
