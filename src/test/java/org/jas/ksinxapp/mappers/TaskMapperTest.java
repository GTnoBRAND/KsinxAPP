package org.jas.ksinxapp.mappers;

import org.jas.ksinxapp.dtos.TaskRequest;
import org.jas.ksinxapp.dtos.TaskResponse;
import org.jas.ksinxapp.model.Modules;
import org.jas.ksinxapp.model.Task;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class TaskMapperTest {

    private final TaskMapper mapper = new TaskMapperImpl();

    @Test
    void toEntity_copiesFields_andIgnoresIdAndModule() {
        var request = new TaskRequest("HW1", "do stuff", 100, LocalDateTime.now().plusDays(7), 5L);

        Task task = mapper.toEntity(request);

        assertThat(task.getId()).isNull();
        assertThat(task.getModule()).isNull();
        assertThat(task.getTitle()).isEqualTo("HW1");
        assertThat(task.getInstructions()).isEqualTo("do stuff");
        assertThat(task.getMaxScore()).isEqualTo(100);
        assertThat(task.getDueDate()).isNotNull();
    }

    @Test
    void toResponse_flattensModuleId() {
        Modules module = new Modules();
        module.setId(77L);
        Task task = new Task(3L, "T", "i", 10, LocalDateTime.of(2026, 1, 1, 0, 0), module);

        TaskResponse response = mapper.toResponse(task);

        assertThat(response.id()).isEqualTo(3L);
        assertThat(response.moduleId()).isEqualTo(77L);
        assertThat(response.maxScore()).isEqualTo(10);
    }
}
