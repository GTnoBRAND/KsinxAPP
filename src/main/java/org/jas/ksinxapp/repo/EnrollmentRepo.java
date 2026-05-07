package org.jas.ksinxapp.repo;

import org.jas.ksinxapp.model.Course;
import org.jas.ksinxapp.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EnrollmentRepo extends JpaRepository<Enrollment,Long> {
    boolean existsByStudentIdAndCourseIdAndIsActiveTrue(Long id, Long id1);
    boolean courseActive(boolean isActive);
}
