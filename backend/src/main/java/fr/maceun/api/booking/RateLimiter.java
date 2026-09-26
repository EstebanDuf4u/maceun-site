package fr.maceun.api.booking;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import fr.maceun.api.config.MaceunProperties;

/** Limite simple en mémoire : N demandes max par IP sur une fenêtre glissante. */
@Component
public class RateLimiter {

    private final MaceunProperties.RateLimit config;
    private final Map<String, Deque<Instant>> hits = new ConcurrentHashMap<>();

    public RateLimiter(MaceunProperties properties) {
        this.config = properties.booking().rateLimit();
    }

    public void check(String key) {
        var now = Instant.now();
        var windowStart = now.minus(config.window());
        var queue = hits.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (queue) {
            while (!queue.isEmpty() && queue.peekFirst().isBefore(windowStart)) {
                queue.pollFirst();
            }
            if (queue.size() >= config.maxRequests()) {
                throw new RateLimitExceededException();
            }
            queue.addLast(now);
        }
        // ménage des IP inactives pour ne pas grossir indéfiniment
        if (hits.size() > 10_000) {
            hits.entrySet().removeIf(e -> e.getValue().isEmpty()
                    || e.getValue().peekLast().isBefore(windowStart));
        }
    }
}
