package fr.maceun.api.event;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ManualEventForm(
        @NotNull(message = "Date obligatoire") LocalDateTime startsAt,
        @NotBlank(message = "Lieu obligatoire") @Size(max = 200) String venue,
        @NotBlank(message = "Ville obligatoire") @Size(max = 120) String city,
        @Size(max = 120) String country,
        @Pattern(regexp = "^$|https?://.+", message = "Lien invalide (doit commencer par https://)") @Size(max = 1000) String ticketUrl
) {}
