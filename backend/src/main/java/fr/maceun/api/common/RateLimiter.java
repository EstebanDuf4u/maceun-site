package fr.maceun.api.common;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Limite simple en mémoire : N demandes max par clé (IP) sur une fenêtre glissante. */
public class RateLimiter {

    private final int maxRequests;
    private final Duration window;
    private final Map<String, Deque<Instant>> hits = new ConcurrentHashMap<>();

    public RateLimiter(int maxRequests, Duration window) {
        this.maxRequests = maxRequests;
        this.window = window;
    }

    public void check(String key) {
        var now = Instant.now();
        var windowStart = now.minus(window);
        var queue = hits.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (queue) {
            while (!queue.isEmpty() && queue.peekFirst().isBefore(windowStart)) {
                queue.pollFirst();
            }
            if (queue.size() >= maxRequests) {
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
