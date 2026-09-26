package fr.maceun.api.booking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import fr.maceun.api.config.MaceunProperties;

/** Transmet la demande de booking à Formspree, qui l'envoie par mail comme sur l'ancien site. */
@Service
public class BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

    private final RestClient client;
    private final String formspreeUrl;

    public BookingService(RestClient.Builder builder, MaceunProperties properties) {
        this.client = builder.build();
        this.formspreeUrl = properties.booking().formspreeUrl();
    }

    public void send(BookingRequest request) {
        var form = new LinkedMultiValueMap<String, String>();
        form.add("nom", request.nom().strip());
        form.add("email", request.email().strip());
        form.add("message", request.message().strip());
        form.add("_replyto", request.email().strip());
        form.add("_subject", "Booking DJ Maceun : " + request.nom().strip());
        try {
            client.post()
                    .uri(formspreeUrl)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(form)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Demande de booking transmise ({})", request.email());
        } catch (RestClientException e) {
            log.error("Échec d'envoi à Formspree : {}", e.getMessage());
            throw new BookingForwardException("Formspree", e);
        }
    }
}
