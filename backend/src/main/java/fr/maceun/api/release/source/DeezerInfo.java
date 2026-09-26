package fr.maceun.api.release.source;

import java.time.LocalDate;

/** Infos d'un son trouvées sur Deezer. */
public record DeezerInfo(LocalDate releaseDate, String cover, String genre, String url) {}
