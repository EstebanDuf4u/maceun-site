package fr.maceun.api.admin;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

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

import fr.maceun.api.release.AdminReleaseView;
import fr.maceun.api.release.ReleaseService;

@RestController
@RequestMapping("/api/admin/releases")
public class AdminReleaseController {

    /** @param manualReleaseDate uniquement pour un son pas encore sorti (sinon la date vient de Deezer) */
    public record AddRelease(@NotBlank(message = "Lien obligatoire") @Size(max = 1000) String url, LocalDate manualReleaseDate) {}

    /** @param url nouveau lien Fanlink (vide = inchangé) */
    public record UpdateRelease(@Size(max = 1000) String url, boolean visible, LocalDate manualReleaseDate) {}

    public record PinRelease(boolean pinned) {}

    private final ReleaseService releases;

    public AdminReleaseController(ReleaseService releases) {
        this.releases = releases;
    }

    @GetMapping
    List<AdminReleaseView> list() {
        return releases.listAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    AdminReleaseView add(@Valid @RequestBody AddRelease body) {
        return releases.add(body.url(), body.manualReleaseDate());
    }

    @PutMapping("/{id}")
    AdminReleaseView update(@PathVariable Long id, @Valid @RequestBody UpdateRelease body) {
        return releases.update(id, body.url(), body.visible(), body.manualReleaseDate());
    }

    @PutMapping("/{id}/pin")
    List<AdminReleaseView> pin(@PathVariable Long id, @RequestBody PinRelease body) {
        return releases.pin(id, body.pinned());
    }

    @PostMapping("/{id}/sync")
    AdminReleaseView sync(@PathVariable Long id) {
        return releases.resync(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable Long id) {
        releases.delete(id);
    }
}
