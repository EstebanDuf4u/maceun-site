package fr.maceun.api.release;

import java.time.LocalDate;

/** @param number numéro affiché sur le site (01 = premier son sorti) */
public record ReleaseView(
        Long id, int number, String title, String subtitle, String genre,
        LocalDate releaseDate, String cover, String url, boolean visible
) {
    static ReleaseView of(Release r, int number) {
        return new ReleaseView(r.getId(), number, r.getTitle(), r.getSubtitle(), r.getGenre(),
                r.getReleaseDate(), r.getCover(), r.getUrl(), r.isVisible());
    }
}
