package fr.maceun.api.content;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.maceun.api.common.NotFoundException;
import fr.maceun.api.storage.StorageService;
import tools.jackson.databind.json.JsonMapper;

@Service
public class ContentService {

    private final SiteContentRepository repository;
    private final JsonMapper json;
    private final StorageService storage;

    /** Le contenu change rarement et est lu à chaque visite : on le garde en mémoire. */
    private volatile SiteContent cached;

    public ContentService(SiteContentRepository repository, JsonMapper json, StorageService storage) {
        this.repository = repository;
        this.json = json;
        this.storage = storage;
    }

    @Transactional(readOnly = true)
    public SiteContent get() {
        var current = cached;
        if (current == null) {
            var entity = repository.findById(SiteContentEntity.ID).orElseThrow(() -> new NotFoundException("Contenu"));
            current = json.readValue(entity.getData(), SiteContent.class);
            cached = current;
        }
        return current;
    }

    @Transactional
    public SiteContent save(SiteContent content) {
        var previous = get();
        var entity = repository.findById(SiteContentEntity.ID).orElseThrow(() -> new NotFoundException("Contenu"));
        entity.setData(json.writeValueAsString(content));
        entity.setUpdatedAt(LocalDateTime.now());
        cached = content;
        // images remplacées : on supprime les anciens fichiers envoyés via l'admin
        previous.images().stream()
                .filter(src -> !content.images().contains(src))
                .forEach(storage::deleteIfUploaded);
        return content;
    }
}
