package org.jas.ksinxapp.repo;

import org.jas.ksinxapp.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepo extends JpaRepository<Course,Long> {

    List<Course> findByIsActiveTrue();
}
