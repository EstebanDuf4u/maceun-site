package fr.maceun.api.release;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.maceun.api.common.NotFoundException;
import fr.maceun.api.release.source.LinkException;
import fr.maceun.api.release.source.LinkScraper;

@Service
public class ReleaseService {

    private final ReleaseRepository repository;
    private final ReleaseSync sync;
    private final LinkScraper scraper;

    public ReleaseService(ReleaseRepository repository, ReleaseSync sync, LinkScraper scraper) {
        this.repository = repository;
        this.sync = sync;
        this.scraper = scraper;
    }

    /**
     * Site public : sons visibles et complets, du plus récent au plus ancien.
     * Mis en avant : le son épinglé s'il y en a un, sinon le plus récent.
     */
    @Transactional(readOnly = true)
    public ReleasesResponse getPublicReleases() {
        var shown = repository.findAllByOrderByReleaseDateDescIdDesc().stream()
                .filter(r -> r.isVisible() && r.getTitle() != null && r.getReleaseDate() != null)
                .toList();
        var views = new ArrayList<ReleaseView>(shown.size());
        int featured = 0;
        for (int i = 0; i < shown.size(); i++) {
            var r = shown.get(i);
            // numéro selon l'ordre de sortie, indépendant de l'épingle
            views.add(new ReleaseView(r.getId(), shown.size() - i, r.getTitle(), r.getGenre(), r.getReleaseDate(),
                    r.getCover(), r.getUrl(), sync.platforms(r)));
            if (r.isPinned()) featured = i;
        }
        if (views.isEmpty()) return new ReleasesResponse(null, List.of());
        var latest = views.remove(featured);
        return new ReleasesResponse(latest, views);
    }

    @Transactional(readOnly = true)
    public List<AdminReleaseView> listAll() {
        return repository.findAllByOrderByReleaseDateDescIdDesc().stream().map(this::toAdmin).toList();
    }

    /** Ajoute un son à partir de son lien ; refusé si le lien est illisible. */
    public AdminReleaseView add(String url, LocalDate manualReleaseDate) {
        var clean = scraper.validate(url).toString();
        if (repository.existsByUrl(clean)) throw new LinkException("Ce son est déjà dans la liste.");
        var release = new Release();
        release.setUrl(clean);
        release.setManualReleaseDate(manualReleaseDate);
        release = sync.sync(release);
        if (release.getTitle() == null) {
            var error = release.getSyncError();
            repository.delete(release);
            throw new LinkException(error != null ? error : "Impossible de lire ce lien.");
        }
        return toAdmin(release);
    }

    /**
     * Modifie un son. Un nouveau lien est lu avant d'être enregistré : s'il est mort ou invalide,
     * l'ancien est conservé et l'erreur est renvoyée.
     */
    public AdminReleaseView update(Long id, String url, boolean visible, LocalDate manualReleaseDate) {
        var release = find(id);
        if (url != null && !url.isBlank()) {
            var clean = scraper.validate(url).toString();
            if (!clean.equals(release.getUrl())) {
                if (repository.existsByUrlAndIdNot(clean, id)) throw new LinkException("Ce lien est déjà utilisé par un autre son.");
                scraper.fetch(clean);
                release.setUrl(clean);
            }
        }
        release.setVisible(visible);
        release.setManualReleaseDate(manualReleaseDate);
        if (!visible) release.setPinned(false);        // un son masqué ne peut pas rester à la une
        return toAdmin(sync.sync(release));
    }

    /** Épingle un son (et désépingle les autres), ou retire l'épingle. */
    @Transactional
    public List<AdminReleaseView> pin(Long id, boolean pinned) {
        var release = find(id);
        if (pinned && !release.isVisible()) throw new LinkException("Affichez d'abord ce son pour pouvoir l'épingler.");
        repository.unpinAll();
        release.setPinned(pinned);
        repository.save(release);
        repository.flush();
        return listAll();
    }

    public AdminReleaseView resync(Long id) {
        return toAdmin(sync.sync(find(id)));
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(find(id));
    }

    private Release find(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Son"));
    }

    private AdminReleaseView toAdmin(Release r) {
        return new AdminReleaseView(r.getId(), r.getUrl(), r.isVisible(), r.isPinned(), r.getManualReleaseDate(), r.getTitle(),
                r.getGenre(), r.getReleaseDate(), r.getCover(), r.getIsrc(), sync.platforms(r), r.getSyncedAt(),
                r.getSyncError());
    }
}
