package fr.maceun.api.event;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/** Date ajoutée à la main depuis l'admin (en plus de celles de Bandsintown). */
@Entity
public class ManualEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime startsAt;
    private String venue;
    private String city;
    private String country;
    private String ticketUrl;

    public Long getId() { return id; }
    public LocalDateTime getStartsAt() { return startsAt; }
    public void setStartsAt(LocalDateTime startsAt) { this.startsAt = startsAt; }
    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getTicketUrl() { return ticketUrl; }
    public void setTicketUrl(String ticketUrl) { this.ticketUrl = ticketUrl; }
}
