package fr.maceun.api.event;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import fr.maceun.api.common.NotFoundException;
import fr.maceun.api.config.MaceunProperties;

/**
 * Prochaines dates = Bandsintown (récupérées côté serveur, en cache) + dates ajoutées depuis l'admin.
 */
@Service
public class EventService {

    private static final Logger log = LoggerFactory.getLogger(EventService.class);
    private static final int MAX_EVENTS = 12;

    private final RestClient client;
    private final MaceunProperties.Bandsintown config;
    private final ManualEventRepository manualEvents;

    private volatile List<EventView> cached = List.of();
    private volatile Instant cachedAt = Instant.EPOCH;

    public EventService(RestClient.Builder builder, MaceunProperties properties, ManualEventRepository manualEvents) {
        this.config = properties.bandsintown();
        this.client = builder.baseUrl(config.baseUrl()).build();
        this.manualEvents = manualEvents;
    }

    @Transactional(readOnly = true)
    public List<EventView> getUpcomingEvents() {
        var manual = manualEvents.findByStartsAtAfterOrderByStartsAt(LocalDate.now().atStartOfDay())
                .stream().map(EventService::toView);
        return Stream.concat(bandsintown().stream(), manual)
                .sorted(Comparator.comparing(EventView::datetime, Comparator.nullsLast(Comparator.naturalOrder())))
                .limit(MAX_EVENTS)
                .toList();
    }

    // ----- admin -----

    @Transactional(readOnly = true)
    public List<EventView> listManual() {
        return manualEvents.findAllByOrderByStartsAtDesc().stream().map(EventService::toView).toList();
    }

    /** Dates Bandsintown, affichées en lecture seule dans l'admin. */
    public List<EventView> listBandsintown() {
        return bandsintown();
    }

    @Transactional
    public EventView create(ManualEventForm form) {
        var event = new ManualEvent();
        apply(event, form);
        return toView(manualEvents.save(event));
    }

    @Transactional
    public EventView update(Long id, ManualEventForm form) {
        var event = manualEvents.findById(id).orElseThrow(() -> new NotFoundException("Date"));
        apply(event, form);
        return toView(event);
    }

    @Transactional
    public void delete(Long id) {
        if (!manualEvents.existsById(id)) throw new NotFoundException("Date");
        manualEvents.deleteById(id);
    }

    // ----- interne -----

    private synchronized List<EventView> bandsintown() {
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
                    .map(EventService::toView)
                    .toList();
            cachedAt = Instant.now();
        } catch (RestClientException e) {
            // on garde l'ancien cache plutôt que de renvoyer une erreur au visiteur
            log.warn("Bandsintown indisponible : {}", e.getMessage());
        }
        return cached;
    }

    private static void apply(ManualEvent e, ManualEventForm f) {
        e.setStartsAt(f.startsAt());
        e.setVenue(f.venue().strip());
        e.setCity(f.city().strip());
        e.setCountry(blankToNull(f.country()));
        e.setTicketUrl(blankToNull(f.ticketUrl()));
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.strip();
    }

    private static EventView toView(ManualEvent e) {
        return new EventView("m-" + e.getId(), e.getStartsAt(), e.getVenue(), e.getCity(), e.getCountry(),
                e.getTicketUrl(), "manual");
    }

    private static EventView toView(BandsintownEvent e) {
        var venue = e.venue();
        return new EventView(
                "b-" + e.id(),
                e.datetime() == null ? null : LocalDateTime.parse(e.datetime()),
                venue == null ? null : venue.name(),
                venue == null ? null : venue.city(),
                venue == null ? null : venue.country(),
                e.url(),
                "bandsintown");
    }
}
