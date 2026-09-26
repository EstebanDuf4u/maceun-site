package fr.maceun.api.photo;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import fr.maceun.api.common.NotFoundException;
import fr.maceun.api.storage.InvalidImageException;
import fr.maceun.api.storage.StorageService;

@Service
public class PhotoService {

    private static final Set<String> SHAPES = Set.of("wide", "tall");

    private final PhotoRepository repository;
    private final StorageService storage;

    public PhotoService(PhotoRepository repository, StorageService storage) {
        this.repository = repository;
        this.storage = storage;
    }

    @Transactional(readOnly = true)
    public List<PhotoView> list() {
        return repository.findAllByOrderByPositionAscIdAsc().stream().map(PhotoView::of).toList();
    }

    /** Ajoute une photo en fin de galerie. Le format est deviné d'après l'image si non précisé. */
    @Transactional
    public PhotoView add(MultipartFile file, String shape) {
        if (shape != null && !shape.isBlank() && !SHAPES.contains(shape)) {
            throw new InvalidImageException("Format d'affichage invalide.");
        }
        var stored = storage.store(file);
        var photo = new Photo();
        photo.setSrc(stored.src());
        photo.setShape(shape != null && !shape.isBlank() ? shape
                : stored.height() != null && stored.height() > stored.width() ? "tall" : "wide");
        photo.setPosition(repository.maxPosition() + 1);
        return PhotoView.of(repository.save(photo));
    }

    @Transactional
    public PhotoView setShape(Long id, String shape) {
        if (!SHAPES.contains(shape)) throw new InvalidImageException("Format d'affichage invalide.");
        var photo = repository.findById(id).orElseThrow(() -> new NotFoundException("Photo"));
        photo.setShape(shape);
        return PhotoView.of(photo);
    }

    /** Réordonne la galerie selon la liste d'ids reçue. */
    @Transactional
    public List<PhotoView> reorder(List<Long> ids) {
        var byId = new HashMap<Long, Photo>();
        repository.findAll().forEach(p -> byId.put(p.getId(), p));
        for (int i = 0; i < ids.size(); i++) {
            var photo = byId.get(ids.get(i));
            if (photo != null) photo.setPosition(i);
        }
        return list();
    }

    @Transactional
    public void delete(Long id) {
        var photo = repository.findById(id).orElseThrow(() -> new NotFoundException("Photo"));
        repository.delete(photo);
        storage.deleteIfUploaded(photo.getSrc());
    }
}
