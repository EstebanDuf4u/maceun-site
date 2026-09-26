package fr.maceun.api.event;

import java.time.LocalDateTime;

/** Date affichée sur le site. {@code source} : "bandsintown" ou "manual" (ajoutée depuis l'admin). */
public record EventView(String id, LocalDateTime datetime, String venue, String city, String country,
                        String ticketUrl, String source) {}
