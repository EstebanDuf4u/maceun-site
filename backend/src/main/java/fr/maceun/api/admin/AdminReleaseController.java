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

import fr.maceun.api.release.ReleaseForm;
import fr.maceun.api.release.ReleaseService;
import fr.maceun.api.release.ReleaseView;

@RestController
@RequestMapping("/api/admin/releases")
public class AdminReleaseController {

    private final ReleaseService releases;

    public AdminReleaseController(ReleaseService releases) {
        this.releases = releases;
    }

    @GetMapping
    List<ReleaseView> list() {
        return releases.listAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ReleaseView create(@Valid @RequestBody ReleaseForm form) {
        return releases.create(form);
    }

    @PutMapping("/{id}")
    ReleaseView update(@PathVariable Long id, @Valid @RequestBody ReleaseForm form) {
        return releases.update(id, form);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable Long id) {
        releases.delete(id);
    }
}
