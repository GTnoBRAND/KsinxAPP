package org.jas.ksinxapp.repo;

import org.jas.ksinxapp.model.Modules;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModulesRepo extends JpaRepository<Modules,Long> {
    //fetch all the lessons for the course ion the correct order!
    List<Modules> findByCourseIdAndIsActiveTrueOrderBySequenceOrderAsc(Long courseId);
}
