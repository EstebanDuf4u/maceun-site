package fr.maceun.api.release.source;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import fr.maceun.api.config.MaceunProperties;
import tools.jackson.databind.json.JsonMapper;

class LinkScraperTest {

    private final LinkScraper scraper = new LinkScraper(RestClient.builder(), JsonMapper.builder().build(),
            new MaceunProperties(null, null, null, null,
                    new MaceunProperties.Releases(List.of("fanlink.tv", "hypeddit.com"), "DJ Maceun", Duration.ofHours(6), false),
                    null));

    @Test
    void readsRealFanlinkPage() throws Exception {
        var html = new String(getClass().getResourceAsStream("/fanlink/feel-your-love.html").readAllBytes(), StandardCharsets.UTF_8);
        var meta = scraper.parse(html);

        assertThat(meta.title()).isEqualTo("Feel Your Love");
        assertThat(meta.isrc()).isEqualTo("SE5BU2634434");
        assertThat(meta.linkCreated()).isEqualTo(java.time.LocalDate.of(2026, 1, 22));
        assertThat(meta.cover()).startsWith("https://i.scdn.co/");
        assertThat(meta.platforms()).extracting(Platform::name).contains("Spotify", "Deezer", "Apple Music", "YouTube");
        // service sans URL (iTunes) ignoré
        assertThat(meta.platforms()).extracting(Platform::name).doesNotContain("iTunes");
    }

    @Test
    void fallsBackToOpenGraph() {
        var meta = scraper.parse("<html><head><meta property=\"og:title\" content=\"I Lose Myself &amp; Co\">"
                + "<meta property=\"og:image\" content=\"https://x.io/c.jpg\"></head></html>");
        assertThat(meta.title()).isEqualTo("I Lose Myself & Co");
        assertThat(meta.cover()).isEqualTo("https://x.io/c.jpg");
    }

    @Test
    void refusesUnknownHostsAndPlainHttp() {
        assertThatThrownBy(() -> scraper.validate("https://evil.example/x")).isInstanceOf(LinkException.class);
        assertThatThrownBy(() -> scraper.validate("http://maceun.fanlink.tv/x")).isInstanceOf(LinkException.class);
        assertThatThrownBy(() -> scraper.validate("https://fanlink.tv.evil.example/x")).isInstanceOf(LinkException.class);
        assertThat(scraper.validate("https://maceun.fanlink.tv/feel-your-love").getHost()).isEqualTo("maceun.fanlink.tv");
    }
}
