package org.jas.ksinxapp.repo;

import org.jas.ksinxapp.dtos.TaskSubmissionRequest;
import org.jas.ksinxapp.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepo extends JpaRepository<Task, Long> {

    //get all homework for a specific lesson
    List<Task> findByModuleId(Long modulesId);
}
