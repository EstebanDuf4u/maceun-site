package fr.maceun.api.release.source;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Sous-ensemble de {@code window.preloadLink} présent dans chaque page Fanlink (ToneDen). */
@JsonIgnoreProperties(ignoreUnknown = true)
record FanlinkPage(
        String title,
        @JsonProperty("metadata_title") String metadataTitle,
        @JsonProperty("image_url") String imageUrl,
        String isrc,
        List<Service> services,
        String createdAt
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    record Service(@JsonProperty("service_name") String serviceName, String url, Boolean active) {}
}
