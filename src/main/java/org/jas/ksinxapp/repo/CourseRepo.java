package org.jas.ksinxapp.repo;

import org.jas.ksinxapp.dtos.CourseSitemapProjection;
import org.jas.ksinxapp.model.Course;
import org.jas.ksinxapp.model.CourseCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepo extends JpaRepository<Course,Long> {
    Page<Course> findByIsActiveTrue(Pageable pageable);
    Page<Course> findByIsActiveTrueAndCategory(CourseCategory category, Pageable pageable);
    List<CourseSitemapProjection> findByIsActiveTrue();
}
