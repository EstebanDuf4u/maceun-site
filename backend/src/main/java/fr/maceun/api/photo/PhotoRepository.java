package fr.maceun.api.photo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PhotoRepository extends JpaRepository<Photo, Long> {

    List<Photo> findAllByOrderByPositionAscIdAsc();

    @Query("select coalesce(max(p.position), -1) from Photo p")
    int maxPosition();
}
