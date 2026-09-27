package fr.maceun.api.release;

import java.time.LocalDate;
import java.util.List;

import fr.maceun.api.release.source.Platform;

/** Son affiché sur le site. {@code number} : 01 = premier son sorti. */
public record ReleaseView(Long id, int number, String title, String genre, LocalDate releaseDate,
                          String cover, String url, List<Platform> platforms) {}
