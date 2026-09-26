package fr.maceun.api.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Sous-ensemble de la réponse Bandsintown qui nous intéresse. */
@JsonIgnoreProperties(ignoreUnknown = true)
record BandsintownEvent(String id, String datetime, String url, Venue venue) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Venue(String name, String city, String country) {}
}
