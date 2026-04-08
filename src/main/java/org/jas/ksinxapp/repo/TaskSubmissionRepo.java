package org.jas.ksinxapp.repo;

import org.jas.ksinxapp.model.TaskSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskSubmissionRepo extends JpaRepository<TaskSubmission,Long> {

    // Find all submissions a student has uploaded
    List<TaskSubmission>  findByStudentId(Long studentId);

    // The Teacher's best friend: Find all homework that still needs a grade!
    List<TaskSubmission>  findByScoreIsNull();
}
