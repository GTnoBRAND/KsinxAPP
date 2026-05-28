package org.jas.ksinxapp.service;

import lombok.RequiredArgsConstructor;
import org.jas.ksinxapp.dtos.ModulesRequest;
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
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepo  taskRepo;
    private final ModulesRepo  modulesRepo;
    private final TaskMapper taskMapper;

    @Transactional
    public TaskResponse createTask(TaskRequest taskRequest) {

        //check if the module entity exists
        Modules modules = modulesRepo.findById(taskRequest.moduleId())
                .orElseThrow(()->new RuntimeException("Module not found!"));

        if(!modules.isActive()){
            throw new RuntimeException("Module is inactive!");
        }

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

        Modules modules = modulesRepo.findById(moduleId)
                .orElseThrow(()->new RuntimeException("Module not found"));
        if(!modules.isActive()){
            throw new RuntimeException("Module is inactive now!");
        }

        return taskRepo.findByModuleId(moduleId)
                .stream()
                .map(taskMapper::toResponse)
                .collect(Collectors.toList());
    }


}
