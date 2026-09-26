package fr.maceun.api.release;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ReleaseForm(
        @NotBlank(message = "Titre obligatoire") @Size(max = 200) String title,
        @Size(max = 200) String subtitle,
        @NotBlank(message = "Genre obligatoire") @Size(max = 120) String genre,
        @NotNull(message = "Date obligatoire") LocalDate releaseDate,
        @NotBlank(message = "Pochette obligatoire") @Size(max = 500) String cover,
        @NotBlank(message = "Lien obligatoire") @Pattern(regexp = "https?://.+", message = "Lien invalide (doit commencer par https://)") @Size(max = 1000) String url,
        Boolean visible
) {
    /** Absent du JSON = visible. */
    public boolean isVisible() {
        return visible == null || visible;
    }
}
