package org.jas.ksinxapp.mappers;

import org.jas.ksinxapp.dtos.TaskSubmissionResponse;
import org.jas.ksinxapp.model.Task;
import org.jas.ksinxapp.model.TaskSubmission;
import org.jas.ksinxapp.model.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class TaskSubmissionMapperTest {

    private final TaskSubmissionMapper mapper = new TaskSubmissionMapperImpl();

    @Test
    void toResponse_flattensNestedTaskAndStudent() {
        Task task = new Task();
        task.setId(10L);
        task.setTitle("HW1");
        User student = new User();
        student.setId(20L);
        student.setFullName("Jane Doe");

        TaskSubmission submission = TaskSubmission.builder()
                .id(1L)
                .task(task)
                .student(student)
                .submittedAt(LocalDateTime.of(2026, 6, 5, 12, 0))
                .fileKey("submissions/abc123_file.pdf")
                .score(85)
                .teacherFeedback("good")
                .build();

        TaskSubmissionResponse response = mapper.toResponse(submission);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.taskId()).isEqualTo(10L);
        assertThat(response.taskTitle()).isEqualTo("HW1");
        assertThat(response.studentId()).isEqualTo(20L);
        assertThat(response.studentFullName()).isEqualTo("Jane Doe");
        assertThat(response.fileKey()).isEqualTo("submissions/abc123_file.pdf");
        assertThat(response.score()).isEqualTo(85);
        assertThat(response.teacherFeedback()).isEqualTo("good");
    }
}
