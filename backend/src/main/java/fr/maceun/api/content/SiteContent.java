package fr.maceun.api.content;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Tout le contenu éditorial du site (textes, liens, images).
 * Dans les textes longs, *mot* s'affiche en italique et les liens / numéros deviennent cliquables.
 */
public record SiteContent(
        @Valid @NotNull Hero hero,
        @Valid @NotNull About about,
        @Valid @NotNull Contact contact,
        @Valid @NotNull @Size(max = 12) List<Social> socials,
        @Valid @NotNull @Size(max = 30) List<Faq> faq
) {
    private static final String URL = "https?://.+";
    private static final String IMAGE = "/(img|uploads)/[\\w.-]+";

    public record Hero(
            @NotBlank(message = "Nom obligatoire") @Size(max = 24) String name,
            @Size(max = 80) String tagline,
            @Size(max = 120) String genres,
            @NotBlank(message = "Image obligatoire") @Pattern(regexp = IMAGE, message = "Image invalide") String image
    ) {}

    public record About(
            @Size(max = 800) String lead,
            @Size(max = 2000) String text,
            @Pattern(regexp = "^$|" + IMAGE, message = "Image invalide") String image,
            @NotNull @Size(max = 20) List<@NotBlank @Size(max = 40) String> tags,
            @Valid @NotNull @Size(max = 4) List<Stat> stats
    ) {}

    public record Stat(
            @NotBlank(message = "Valeur obligatoire") @Size(max = 12) String value,
            @NotBlank(message = "Libellé obligatoire") @Size(max = 40) String label
    ) {}

    public record Contact(
            @Size(max = 30) String phone,
            @Size(max = 500) String bookingText,
            @Pattern(regexp = "^$|" + URL, message = "Lien invalide") @Size(max = 500) String streamingUrl,
            @Pattern(regexp = "^$|" + IMAGE, message = "Image invalide") String socialImage
    ) {}

    public record Social(
            @NotBlank(message = "Nom obligatoire") @Size(max = 30) String label,
            @NotBlank(message = "Lien obligatoire") @Pattern(regexp = URL, message = "Lien invalide") @Size(max = 500) String url
    ) {}

    public record Faq(
            @NotBlank(message = "Question obligatoire") @Size(max = 200) String question,
            @NotBlank(message = "Réponse obligatoire") @Size(max = 1500) String answer
    ) {}

    /** Images référencées, pour supprimer celles qui ne servent plus. */
    List<String> images() {
        return java.util.stream.Stream.of(hero.image(), about.image(), contact.socialImage())
                .filter(java.util.Objects::nonNull).toList();
    }
}
