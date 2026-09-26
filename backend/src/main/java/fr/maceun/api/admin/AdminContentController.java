package fr.maceun.api.admin;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.maceun.api.content.ContentService;
import fr.maceun.api.content.SiteContent;

@RestController
@RequestMapping("/api/admin/content")
public class AdminContentController {

    private final ContentService content;

    public AdminContentController(ContentService content) {
        this.content = content;
    }

    @GetMapping
    SiteContent get() {
        return content.get();
    }

    @PutMapping
    SiteContent save(@Valid @RequestBody SiteContent body) {
        return content.save(body);
    }
}
