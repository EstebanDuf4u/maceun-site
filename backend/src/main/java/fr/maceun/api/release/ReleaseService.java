package fr.maceun.api.release;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

/**
 * Discographie. Pour ajouter un son : une ligne dans la liste, la sortie la plus récente
 * devient automatiquement la "latest".
 */
@Service
public class ReleaseService {

    private static final String TECHNO = "Techno / Hard Techno";

    private final List<Release> releases = List.of(
            new Release("i-lose-myself", 5, "I Lose Myself", null, "Techno",
                    LocalDate.of(2026, 10, 2), "/img/i-lose-myself.jpg", "https://hypeddit.com/qbfvvm"),
            new Release("everything-is-romantic", 4, "Everything is romantic", "Remix Techno", TECHNO,
                    LocalDate.of(2026, 6, 1), "/img/everything-is-romantic.jpg", "https://maceun.fanlink.tv/everything_is_romantic"),
            new Release("salle-vide", 3, "Salle Vide", "Techno Remix", TECHNO,
                    LocalDate.of(2026, 4, 1), "/img/salle-vide.jpg", "https://maceun.fanlink.tv/salle-vide"),
            new Release("safe-and-sound", 2, "Safe And Sound", null, TECHNO,
                    LocalDate.of(2026, 2, 1), "/img/safe-and-sound.jpg", "https://fanlink.tv/safe-and-sound-maceun"),
            new Release("feel-your-love", 1, "Feel Your Love", null, TECHNO,
                    LocalDate.of(2026, 1, 1), "/img/feel-your-love.jpg", "https://maceun.fanlink.tv/feel-your-love")
    );

    public ReleasesResponse getReleases() {
        var sorted = releases.stream()
                .sorted(Comparator.comparing(Release::releaseDate).reversed())
                .toList();
        return new ReleasesResponse(sorted.getFirst(), sorted.subList(1, sorted.size()));
    }
}
