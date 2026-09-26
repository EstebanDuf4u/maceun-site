package fr.maceun.api.release;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.maceun.api.common.NotFoundException;
import fr.maceun.api.storage.StorageService;

@Service
public class ReleaseService {

    private final ReleaseRepository repository;
    private final StorageService storage;

    public ReleaseService(ReleaseRepository repository, StorageService storage) {
        this.repository = repository;
        this.storage = storage;
    }

    /** Site public : sorties visibles, la plus récente devient la "latest". */
    @Transactional(readOnly = true)
    public ReleasesResponse getPublicReleases() {
        var views = numbered(repository.findByVisibleTrueOrderByReleaseDateDesc());
        if (views.isEmpty()) return new ReleasesResponse(null, List.of());
        return new ReleasesResponse(views.getFirst(), views.subList(1, views.size()));
    }

    @Transactional(readOnly = true)
    public List<ReleaseView> listAll() {
        return numbered(repository.findAllByOrderByReleaseDateDesc());
    }

    @Transactional
    public ReleaseView create(ReleaseForm form) {
        var release = new Release();
        apply(release, form);
        repository.save(release);
        return find(release.getId());
    }

    @Transactional
    public ReleaseView update(Long id, ReleaseForm form) {
        var release = repository.findById(id).orElseThrow(() -> new NotFoundException("Sortie"));
        var oldCover = release.getCover();
        apply(release, form);
        if (!oldCover.equals(form.cover())) storage.deleteIfUploaded(oldCover);
        return find(id);
    }

    @Transactional
    public void delete(Long id) {
        var release = repository.findById(id).orElseThrow(() -> new NotFoundException("Sortie"));
        repository.delete(release);
        storage.deleteIfUploaded(release.getCover());
    }

    private ReleaseView find(Long id) {
        return listAll().stream().filter(r -> r.id().equals(id)).findFirst()
                .orElseThrow(() -> new NotFoundException("Sortie"));
    }

    private static void apply(Release r, ReleaseForm f) {
        r.setTitle(f.title().strip());
        r.setSubtitle(f.subtitle() == null || f.subtitle().isBlank() ? null : f.subtitle().strip());
        r.setGenre(f.genre().strip());
        r.setReleaseDate(f.releaseDate());
        r.setCover(f.cover().strip());
        r.setUrl(f.url().strip());
        r.setVisible(f.isVisible());
    }

    /** Liste triée du plus récent au plus ancien → numéro 01 pour le plus ancien. */
    private static List<ReleaseView> numbered(List<Release> newestFirst) {
        var out = new ArrayList<ReleaseView>(newestFirst.size());
        for (int i = 0; i < newestFirst.size(); i++) {
            out.add(ReleaseView.of(newestFirst.get(i), newestFirst.size() - i));
        }
        return out;
    }
}
