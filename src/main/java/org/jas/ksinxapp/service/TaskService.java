package org.jas.ksinxapp.service;

import org.jas.ksinxapp.dtos.TaskRequest;
import org.jas.ksinxapp.dtos.TaskResponse;
import org.jas.ksinxapp.mappers.TaskMapper;
import org.jas.ksinxapp.model.Modules;
import org.jas.ksinxapp.model.Task;
import org.jas.ksinxapp.repo.ModulesRepo;
import org.jas.ksinxapp.repo.TaskRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final TaskRepo  taskRepo;
    private final TaskMapper taskMapper;
    private final ModulesRepo modulesRepo;

    public TaskService(TaskRepo taskRepo, TaskMapper taskMapper,  ModulesRepo modulesRepo) {
        this.taskRepo = taskRepo;
        this.taskMapper = taskMapper;
        this.modulesRepo = modulesRepo;
    }

    @Transactional
    public TaskResponse createTask(TaskRequest taskRequest) {

        //check if the module entity exists
        Modules modules = modulesRepo.findById(taskRequest.moduleId())
                .orElseThrow(()->new RuntimeException("Module not found!"));

        //map the basic fields
        Task task = taskMapper.toEntity(taskRequest);

        task.setModule(modules);

        //save to postgresql
        Task savedTask = taskRepo.save(task);

        //return the dto
        return taskMapper.toResponse(savedTask);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksForModule(Long moduleId) {

        return taskRepo.findByModuleId(moduleId)
                .stream()
                .map(taskMapper::toResponse)
                .collect(Collectors.toList());
    }


}
