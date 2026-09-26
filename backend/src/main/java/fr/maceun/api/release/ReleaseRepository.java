package fr.maceun.api.release;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReleaseRepository extends JpaRepository<Release, Long> {

    List<Release> findAllByOrderByReleaseDateDesc();

    List<Release> findByVisibleTrueOrderByReleaseDateDesc();
}
