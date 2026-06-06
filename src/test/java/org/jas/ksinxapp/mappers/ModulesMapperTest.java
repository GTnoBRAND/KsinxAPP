package org.jas.ksinxapp.mappers;

import org.jas.ksinxapp.dtos.ModulesRequest;
import org.jas.ksinxapp.dtos.ModulesResponse;
import org.jas.ksinxapp.model.Course;
import org.jas.ksinxapp.model.Modules;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ModulesMapperTest {

    private final ModulesMapper mapper = new ModulesMapperImpl();

    @Test
    void toEntity_copiesFields_andIgnoresIdAndCourse() {
        var request = new ModulesRequest("Module 1", 1, "https://video", 42L);

        Modules module = mapper.toEntity(request);

        assertThat(module.getId()).isNull();
        assertThat(module.getCourse()).isNull();
        assertThat(module.getTitle()).isEqualTo("Module 1");
        assertThat(module.getSequenceOrder()).isEqualTo(1);
        assertThat(module.getVideoUrl()).isEqualTo("https://video");
    }

    @Test
    void toResponse_flattensCourseId() {
        Course course = new Course();
        course.setId(99L);
        Modules module = new Modules(5L, "M", 2, "/v", course);

        ModulesResponse response = mapper.toResponse(module);

        assertThat(response.id()).isEqualTo(5L);
        assertThat(response.title()).isEqualTo("M");
        assertThat(response.sequenceOrder()).isEqualTo(2);
        assertThat(response.videoUrl()).isEqualTo("/v");
        assertThat(response.courseId()).isEqualTo(99L);
    }
}
