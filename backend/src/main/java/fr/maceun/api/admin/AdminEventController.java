package fr.maceun.api.admin;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import fr.maceun.api.event.EventService;
import fr.maceun.api.event.EventView;
import fr.maceun.api.event.ManualEventForm;

@RestController
@RequestMapping("/api/admin/events")
public class AdminEventController {

    private final EventService events;

    public AdminEventController(EventService events) {
        this.events = events;
    }

    /** Dates ajoutées à la main (modifiables). */
    @GetMapping
    List<EventView> list() {
        return events.listManual();
    }

    /** Dates venant de Bandsintown (lecture seule, à gérer sur Bandsintown). */
    @GetMapping("/bandsintown")
    List<EventView> bandsintown() {
        return events.listBandsintown();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    EventView create(@Valid @RequestBody ManualEventForm form) {
        return events.create(form);
    }

    @PutMapping("/{id}")
    EventView update(@PathVariable Long id, @Valid @RequestBody ManualEventForm form) {
        return events.update(id, form);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable Long id) {
        events.delete(id);
    }
}
