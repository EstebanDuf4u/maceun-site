package fr.maceun.api.booking;

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/booking")
public class BookingController {

    private final BookingService bookingService;
    private final RateLimiter rateLimiter;

    public BookingController(BookingService bookingService, RateLimiter rateLimiter) {
        this.bookingService = bookingService;
        this.rateLimiter = rateLimiter;
    }

    @PostMapping
    ResponseEntity<Map<String, String>> book(@Valid @RequestBody BookingRequest request, HttpServletRequest http) {
        // bot détecté : on fait comme si tout allait bien, sans rien envoyer
        if (request.website() != null && !request.website().isBlank()) {
            return ResponseEntity.accepted().body(Map.of("status", "ok"));
        }
        rateLimiter.check(http.getRemoteAddr());
        bookingService.send(request);
        return ResponseEntity.accepted().body(Map.of("status", "ok"));
    }
}
