package fr.maceun.api.booking;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * @param website champ piège invisible : un humain le laisse vide, un bot le remplit.
 */
public record BookingRequest(
        @NotBlank(message = "Nom obligatoire") @Size(max = 120) String nom,
        @NotBlank(message = "Email obligatoire") @Email(message = "Email invalide") @Size(max = 200) String email,
        @NotBlank(message = "Message obligatoire") @Size(min = 10, max = 5000, message = "Message trop court") String message,
        String website
) {}
