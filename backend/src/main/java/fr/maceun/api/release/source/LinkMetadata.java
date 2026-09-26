package fr.maceun.api.release.source;

import java.time.LocalDate;
import java.util.List;

/**
 * Ce qu'on sait lire sur la page d'un lien (Fanlink ou autre smart link).
 * @param linkCreated date de création du lien : date de sortie approximative si Deezer ne connaît pas le son
 */
public record LinkMetadata(String title, String cover, String isrc, List<Platform> platforms, LocalDate linkCreated) {

    public LinkMetadata(String title, String cover, String isrc, List<Platform> platforms) {
        this(title, cover, isrc, platforms, null);
    }
}
