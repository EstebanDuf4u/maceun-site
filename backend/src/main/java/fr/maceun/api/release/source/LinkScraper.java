package fr.maceun.api.release.source;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import fr.maceun.api.config.MaceunProperties;
import tools.jackson.databind.json.JsonMapper;

/**
 * Lit la page publique d'un lien Fanlink : titre, pochette, ISRC et liens vers les plateformes
 * (données embarquées dans {@code window.preloadLink}). Pour un autre type de lien, on se rabat
 * sur les balises Open Graph (og:title, og:image).
 */
@Component
public class LinkScraper {

    private static final Pattern PRELOAD = Pattern.compile("window\\.preloadLink\\s*=\\s*(\\{.*?\\});\\s*\\n", Pattern.DOTALL);
    private static final Map<String, String> PLATFORM_NAMES = Map.ofEntries(
            Map.entry("spotify", "Spotify"), Map.entry("deezer", "Deezer"), Map.entry("apple-music", "Apple Music"),
            Map.entry("itunes", "iTunes"), Map.entry("youtube", "YouTube"), Map.entry("youtube-music", "YouTube Music"),
            Map.entry("soundcloud", "SoundCloud"), Map.entry("amazon-music", "Amazon Music"), Map.entry("tidal", "Tidal"),
            Map.entry("beatport", "Beatport"), Map.entry("audiomack", "Audiomack"), Map.entry("napster", "Napster"));

    private final RestClient client;
    private final JsonMapper json;
    private final List<String> allowedHosts;

    public LinkScraper(RestClient.Builder builder, JsonMapper json, MaceunProperties properties) {
        this.client = builder.defaultHeader("User-Agent", "Mozilla/5.0 (compatible; MaceunSite/1.0)").build();
        this.json = json;
        this.allowedHosts = properties.releases().allowedHosts();
    }

    /** Vérifie qu'un lien est en https et sur un domaine autorisé (sinon on pourrait interroger n'importe quel serveur). */
    public URI validate(String url) {
        URI uri;
        try {
            uri = URI.create(url.strip());
        } catch (IllegalArgumentException e) {
            throw new LinkException("Lien invalide.");
        }
        var host = uri.getHost() == null ? "" : uri.getHost().toLowerCase();
        if (!"https".equals(uri.getScheme()) || allowedHosts.stream().noneMatch(h -> host.equals(h) || host.endsWith("." + h))) {
            throw new LinkException("Lien non accepté : utilisez un lien Fanlink (https://….fanlink.tv/…).");
        }
        return uri;
    }

    public LinkMetadata fetch(String url) {
        var uri = validate(url);
        String html;
        try {
            html = client.get().uri(uri).retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        throw new LinkException("La page du lien répond « " + res.getStatusCode().value() + " » (lien mort ?).");
                    })
                    .body(String.class);
        } catch (RestClientException e) {
            throw new LinkException("Page du lien injoignable.");
        }
        if (html == null) throw new LinkException("Page du lien vide.");
        return parse(html);
    }

    /** Extrait les infos du HTML d'une page de lien. */
    LinkMetadata parse(String html) {
        var preload = PRELOAD.matcher(html);
        if (preload.find()) return fromFanlink(json.readValue(preload.group(1), FanlinkPage.class));
        return fromOpenGraph(html);
    }

    private static LinkMetadata fromFanlink(FanlinkPage page) {
        var title = firstNonBlank(page.title(), page.metadataTitle());
        if (title == null) throw new LinkException("Titre introuvable sur la page Fanlink.");
        var platforms = page.services() == null ? List.<Platform>of() : page.services().stream()
                .filter(s -> !Boolean.FALSE.equals(s.active()) && s.url() != null && s.url().startsWith("http"))
                .map(s -> new Platform(PLATFORM_NAMES.getOrDefault(s.serviceName(), capitalize(s.serviceName())), s.url()))
                .toList();
        var created = page.createdAt() == null || page.createdAt().length() < 10 ? null
                : java.time.LocalDate.parse(page.createdAt().substring(0, 10));
        return new LinkMetadata(title, blankToNull(page.imageUrl()), blankToNull(page.isrc()), platforms, created);
    }

    private static LinkMetadata fromOpenGraph(String html) {
        var title = meta(html, "og:title");
        if (title == null) throw new LinkException("Aucune info de son trouvée sur cette page.");
        return new LinkMetadata(title, meta(html, "og:image"), null, List.of());
    }

    private static String meta(String html, String property) {
        var m = Pattern.compile("<meta[^>]+property=\"" + Pattern.quote(property) + "\"[^>]+content=\"([^\"]*)\"").matcher(html);
        return m.find() ? blankToNull(org.springframework.web.util.HtmlUtils.htmlUnescape(m.group(1))) : null;
    }

    private static String firstNonBlank(String... values) {
        for (var v : values) if (v != null && !v.isBlank()) return v.strip();
        return null;
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.strip();
    }

    private static String capitalize(String s) {
        return s == null || s.isEmpty() ? "Écouter" : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

}
