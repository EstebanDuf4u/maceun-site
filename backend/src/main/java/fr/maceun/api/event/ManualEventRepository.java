package fr.maceun.api.event;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ManualEventRepository extends JpaRepository<ManualEvent, Long> {

    List<ManualEvent> findAllByOrderByStartsAtDesc();

    List<ManualEvent> findByStartsAtAfterOrderByStartsAt(LocalDateTime after);
}
