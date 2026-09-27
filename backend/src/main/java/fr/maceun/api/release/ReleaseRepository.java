package fr.maceun.api.release;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ReleaseRepository extends JpaRepository<Release, Long> {

    List<Release> findAllByOrderByReleaseDateDescIdDesc();

    boolean existsByUrl(String url);

    boolean existsByUrlAndIdNot(String url, Long id);

    @Modifying
    @Query("update Release r set r.pinned = false where r.pinned = true")
    void unpinAll();
}
