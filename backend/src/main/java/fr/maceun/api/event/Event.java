package fr.maceun.api.event;

import java.time.LocalDateTime;

public record Event(String id, LocalDateTime datetime, String venue, String city, String country, String ticketUrl) {}
