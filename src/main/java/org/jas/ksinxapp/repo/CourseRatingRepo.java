package org.jas.ksinxapp.repo;

import org.jas.ksinxapp.model.CourseRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CourseRatingRepo extends JpaRepository<CourseRating, Long> {

    Optional<CourseRating> findByCourseIdAndUserId(Long courseId, Long userId);

    @Query("select coalesce(avg(r.rating), 0.0) from CourseRating r where r.courseId = :courseId")
    Double findAverageByCourseId(Long courseId);

    long countByCourseId(Long courseId);
}
