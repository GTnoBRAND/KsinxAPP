//package org.jas.ksinxapp.mappers;
//
//import org.jas.ksinxapp.dtos.CourseCreateRequest;
//import org.jas.ksinxapp.dtos.CourseResponse;
//import org.jas.ksinxapp.model.Course;
//import org.jas.ksinxapp.model.Modules;
//import org.junit.jupiter.api.Test;
//
//import java.math.BigDecimal;
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//class CourseMapperTest {
//
//    private final CourseMapper mapper = new CourseMapperImpl();
//
//    @Test
//    void toEntity_copiesFields_andIgnoresIdAndMedia() {
//        var request = new CourseCreateRequest("Java 101", "intro course", new BigDecimal("19.99"));
//
//        Course course = mapper.toEntity(request);
//
//        assertThat(course.getId()).isNull();
//        assertThat(course.getTitle()).isEqualTo("Java 101");
//        assertThat(course.getDescription()).isEqualTo("intro course");
//        assertThat(course.getPrice()).isEqualByComparingTo("19.99");
//        assertThat(course.getImageUrl()).isNull();
//        assertThat(course.getVideoUrl()).isNull();
//        assertThat(course.getModules()).isNull();
//    }
//
//    @Test
//    void toResponse_countsModules() {
//        Course course = new Course();
//        course.setId(11L);
//        course.setTitle("Spring");
//        course.setDescription("d");
//        course.setPrice(new BigDecimal("50"));
//        course.setImageUrl("/img.png");
//        course.setVideoUrl("/vid.mp4");
//        course.setModules(List.of(new Modules(), new Modules(), new Modules()));
//
//        CourseResponse response = mapper.toResponse(course);
//
//        assertThat(response.id()).isEqualTo(11L);
//        assertThat(response.totalModules()).isEqualTo(3);
//        assertThat(response.imageUrl()).isEqualTo("/img.png");
//        assertThat(response.videoUrl()).isEqualTo("/vid.mp4");
//    }
//
//    @Test
//    void toResponse_zeroModulesWhenNull() {
//        Course course = new Course();
//        course.setId(1L);
//        course.setTitle("t");
//        course.setDescription("d");
//        course.setPrice(BigDecimal.ZERO);
//
//        CourseResponse response = mapper.toResponse(course);
//
//        assertThat(response.totalModules()).isZero();
//    }
//
//    @Test
//    void updateEntityFromDto_replacesEditableFields_butLeavesMediaUntouched() {
//        Course course = new Course();
//        course.setId(1L);
//        course.setImageUrl("/old.png");
//        course.setVideoUrl("/old.mp4");
//        var request = new CourseCreateRequest("New", "newd", new BigDecimal("99"));
//
//        mapper.updateEntityFromDto(request, course);
//
//        assertThat(course.getTitle()).isEqualTo("New");
//        assertThat(course.getDescription()).isEqualTo("newd");
//        assertThat(course.getPrice()).isEqualByComparingTo("99");
//        assertThat(course.getImageUrl()).isEqualTo("/old.png");
//        assertThat(course.getVideoUrl()).isEqualTo("/old.mp4");
//    }
//
//    @Test
//    void listToResponse_mapsEachElement() {
//        Course a = new Course();
//        a.setId(1L);
//        a.setTitle("a");
//        a.setDescription("a");
//        a.setPrice(BigDecimal.ONE);
//        Course b = new Course();
//        b.setId(2L);
//        b.setTitle("b");
//        b.setDescription("b");
//        b.setPrice(BigDecimal.TEN);
//
//        List<CourseResponse> responses = mapper.ListToResponse(List.of(a, b));
//
//        assertThat(responses).extracting(CourseResponse::id).containsExactly(1L, 2L);
//    }
//}
