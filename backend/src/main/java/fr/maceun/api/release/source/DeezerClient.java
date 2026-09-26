package fr.maceun.api.release.source;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import fr.maceun.api.config.MaceunProperties;

/**
 * API publique Deezer (sans clé) : date de sortie, pochette HD et genre d'un son,
 * retrouvé par son ISRC, sinon par titre + nom d'artiste.
 */
@Component
public class DeezerClient {

    private static final Logger log = LoggerFactory.getLogger(DeezerClient.class);
    private static final int QUOTA_EXCEEDED = 4;

    private final RestClient client;
    private final String artist;

    public DeezerClient(RestClient.Builder builder, MaceunProperties properties) {
        this.client = builder.baseUrl("https://api.deezer.com").build();
        this.artist = properties.releases().deezerArtist();
    }

    public Optional<DeezerInfo> find(String isrc, String title) {
        try {
            var track = isrc != null ? get("/track/isrc:{isrc}", isrc) : null;
            if (track == null && title != null) track = search(title);
            if (track == null || track.album() == null) return Optional.empty();
            return Optional.of(new DeezerInfo(
                    track.releaseDate() == null ? null : LocalDate.parse(track.releaseDate()),
                    track.album().coverXl(),
                    genres(track.album().id()),
                    track.link()));
        } catch (RestClientException e) {
            log.warn("Deezer indisponible : {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Recherche simple « artiste titre » (la recherche avancée artist:"…" ne trouve pas les petits artistes),
     * puis on garde le résultat du bon artiste dont le titre commence pareil (parenthèses ignorées).
     */
    private Track search(String title) {
        var cleanTitle = title.replaceAll("\\s*[(\\[].*?[)\\]]", "").strip().toLowerCase();
        var result = withQuotaRetry(() -> client.get()
                .uri(u -> u.path("/search").queryParam("q", artist + " " + cleanTitle).build())
                .retrieve().body(SearchResult.class), SearchResult::error);
        if (result == null || result.data() == null) return null;
        return result.data().stream()
                .filter(t -> t.artist() != null && artist.equalsIgnoreCase(t.artist().name()))
                .filter(t -> t.title() != null && t.title().toLowerCase().startsWith(cleanTitle))
                .findFirst()
                .map(t -> get("/track/{id}", String.valueOf(t.id())))   // la recherche ne donne pas la date
                .orElse(null);
    }

    private Track get(String path, String param) {
        var track = withQuotaRetry(() -> client.get().uri(path, param).retrieve().body(Track.class), Track::error);
        return track == null || track.error() != null ? null : track;
    }

    /**
     * Deezer limite à 50 requêtes / 5 s et répond alors « 200 » avec {"error":{"code":4}}.
     * Sans ça, un son serait considéré à tort comme absent de Deezer.
     */
    private <T> T withQuotaRetry(java.util.function.Supplier<T> call, java.util.function.Function<T, Error> errorOf) {
        for (int attempt = 1; ; attempt++) {
            var result = call.get();
            var error = result == null ? null : errorOf.apply(result);
            if (error == null || error.code() == null || error.code() != QUOTA_EXCEEDED || attempt == 3) return result;
            log.info("Quota Deezer atteint, nouvel essai dans {} s", attempt * 2);
            try {
                Thread.sleep(attempt * 2000L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return result;
            }
        }
    }

    private String genres(Long albumId) {
        if (albumId == null) return null;
        var album = withQuotaRetry(() -> client.get().uri("/album/{id}", albumId).retrieve().body(Album.class), Album::error);
        if (album == null || album.genres() == null || album.genres().data() == null) return null;
        var names = album.genres().data().stream().map(Genre::name).collect(Collectors.joining(" / "));
        return names.isBlank() ? null : names;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Track(Long id, String title, String link, @JsonProperty("release_date") String releaseDate,
                 TrackAlbum album, Artist artist, Error error) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Error(Integer code, String message) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record TrackAlbum(Long id, @JsonProperty("cover_xl") String coverXl) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Artist(String name) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record SearchResult(List<Track> data, Error error) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Album(Genres genres, Error error) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Genres(List<Genre> data) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Genre(String name) {}
}
