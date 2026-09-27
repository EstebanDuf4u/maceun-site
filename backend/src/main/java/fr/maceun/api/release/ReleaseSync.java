package fr.maceun.api.release;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import fr.maceun.api.config.MaceunProperties;
import fr.maceun.api.release.source.DeezerClient;
import fr.maceun.api.release.source.DeezerInfo;
import fr.maceun.api.release.source.LinkException;
import fr.maceun.api.release.source.LinkScraper;
import fr.maceun.api.release.source.Platform;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * Remplit un son depuis son lien Fanlink (titre, pochette, plateformes) puis Deezer (date, genre, pochette HD).
 * En cas d'échec, les dernières infos connues sont gardées et l'erreur est affichée dans l'admin.
 */
@Component
public class ReleaseSync {

    private static final Logger log = LoggerFactory.getLogger(ReleaseSync.class);

    private final ReleaseRepository repository;
    private final LinkScraper scraper;
    private final DeezerClient deezer;
    private final JsonMapper json;
    private final TaskScheduler scheduler;
    private final MaceunProperties.Releases config;

    public ReleaseSync(ReleaseRepository repository, LinkScraper scraper, DeezerClient deezer, JsonMapper json,
                       TaskScheduler scheduler, MaceunProperties properties) {
        this.repository = repository;
        this.scraper = scraper;
        this.deezer = deezer;
        this.json = json;
        this.scheduler = scheduler;
        this.config = properties.releases();
    }

    /** Au démarrage, puis toutes les {@code sync-interval} (6 h par défaut). */
    @EventListener(ApplicationReadyEvent.class)
    void schedule() {
        if (!config.autoSync()) return;
        scheduler.scheduleWithFixedDelay(this::syncAll, java.time.Instant.now().plusSeconds(5), config.syncInterval());
    }

    public void syncAll() {
        var releases = repository.findAll();
        for (var release : releases) {
            sync(release);
            pause();       // ménage les API Fanlink / Deezer
        }
        log.info("Sons synchronisés : {} (erreurs : {})", releases.size(),
                releases.stream().filter(r -> r.getSyncError() != null).count());
    }

    /** Met à jour un son et l'enregistre. Ne lève jamais d'exception : l'erreur est stockée. */
    public Release sync(Release release) {
        try {
            var meta = scraper.fetch(release.getUrl());
            release.setTitle(meta.title());
            release.setIsrc(meta.isrc());
            if (meta.cover() != null) release.setCover(meta.cover());
            var platforms = new ArrayList<>(meta.platforms());

            var found = deezer.find(meta.isrc(), meta.title());
            if (found.isPresent()) {
                var info = found.get();
                if (info.releaseDate() != null) release.setReleaseDate(info.releaseDate());
                if (info.cover() != null) release.setCover(info.cover());       // 1000 px, meilleure qualité
                if (info.genre() != null) release.setGenre(info.genre());
                if (info.url() != null && platforms.stream().noneMatch(p -> p.name().equals("Deezer"))) {
                    platforms.add(new Platform("Deezer", info.url()));
                }
            }
            // Deezer ne connaît pas le son : date saisie dans l'admin, sinon date de création du lien Fanlink
            if (found.map(DeezerInfo::releaseDate).isEmpty()) {
                if (release.getManualReleaseDate() != null) release.setReleaseDate(release.getManualReleaseDate());
                else if (meta.linkCreated() != null) release.setReleaseDate(meta.linkCreated());
            }
            release.setPlatforms(json.writeValueAsString(platforms));
            release.setSyncError(null);
        } catch (LinkException e) {
            release.setSyncError(e.getMessage());
        } catch (RuntimeException e) {
            log.warn("Synchronisation de {} impossible", release.getUrl(), e);
            release.setSyncError("Erreur inattendue pendant la mise à jour.");
        }
        if (release.getReleaseDate() == null) release.setReleaseDate(release.getManualReleaseDate());
        release.setSyncedAt(LocalDateTime.now());
        return repository.save(release);
    }

    private static void pause() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    List<Platform> platforms(Release release) {
        if (release.getPlatforms() == null) return List.of();
        return json.readValue(release.getPlatforms(), new TypeReference<List<Platform>>() {});
    }
}
