package fr.maceun.api.event;

import java.time.LocalDateTime;

public record EventView(String id, LocalDateTime datetime, String venue, String city, String country, String ticketUrl) {}
