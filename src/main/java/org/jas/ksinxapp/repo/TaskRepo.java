package org.jas.ksinxapp.repo;

import org.jas.ksinxapp.dtos.TaskSubmissionRequest;
import org.jas.ksinxapp.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepo extends JpaRepository<Task, Long> {

    //get all homework for a specific lesson
    List<Task> findByModuleId(Long modulesId);
    int countByModule_CourseId(Long courseId);

    //count average score
    @Query("SELECT AVG(ts.score) FROM TaskSubmission ts " +
    "WHERE ts.student.id = :studentId " +
    "AND ts.task.module.course.id = :courseId " +
    "AND ts.score IS NOT NULL ")
    Double averageScoreByStudentInCourse(@Param("studentId") Long studentId,
                                         @Param("courseId") Long courseId);
}
