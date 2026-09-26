package fr.maceun.api.release;

import java.time.LocalDate;

public record Release(
        String slug,
        int number,
        String title,
        String subtitle,
        String genre,
        LocalDate releaseDate,
        String cover,
        String url
) {}
