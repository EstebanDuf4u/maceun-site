package fr.maceun.api.release;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import fr.maceun.api.release.source.Platform;

/** Son vu dans l'admin : infos récupérées + état de la synchronisation. */
public record AdminReleaseView(Long id, String url, boolean visible, boolean pinned, LocalDate manualReleaseDate,
                               String title, String genre, LocalDate releaseDate, String cover, String isrc,
                               List<Platform> platforms, LocalDateTime syncedAt, String syncError) {}
