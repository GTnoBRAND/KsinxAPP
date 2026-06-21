//package org.jas.ksinxapp.mappers;
//
//import org.jas.ksinxapp.dtos.EnrollmentResponse;
//import org.jas.ksinxapp.model.Course;
//import org.jas.ksinxapp.model.Enrollment;
//import org.jas.ksinxapp.model.User;
//import org.junit.jupiter.api.Test;
//
//import java.time.LocalDateTime;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//class EnrollmentMapperTest {
//
//    private final EnrollmentMapper mapper = new EnrollmentMapperImpl();
//
//    @Test
//    void toResponseDto_initializesProgressFieldsToZero() {
//        User student = new User();
//        student.setId(1L);
//        student.setFullName("Jane");
//        Course course = new Course();
//        course.setId(2L);
//        course.setTitle("Java");
//
//        Enrollment enrollment = Enrollment.builder()
//                .id(99L)
//                .student(student)
//                .course(course)
//                .enrollmentDate(LocalDateTime.of(2026, 1, 1, 0, 0))
//                .isActive(true)
//                .build();
//
//        EnrollmentResponse response = mapper.toResponseDto(enrollment);
//
//        assertThat(response.enrollmentId()).isEqualTo(99L);
//        assertThat(response.studentId()).isEqualTo(1L);
//        assertThat(response.studentFullName()).isEqualTo("Jane");
//        assertThat(response.courseId()).isEqualTo(2L);
//        assertThat(response.courseTitle()).isEqualTo("Java");
//        assertThat(response.isActive()).isTrue();
//        assertThat(response.totalTasks()).isZero();
//        assertThat(response.totalCompletedTasks()).isZero();
//        assertThat(response.completionPercentage()).isZero();
//    }
//
//    @Test
//    void toResponse_includesProgressFields() {
//        User student = new User();
//        student.setId(1L);
//        student.setFullName("Jane");
//        Course course = new Course();
//        course.setId(2L);
//        course.setTitle("Java");
//        Enrollment enrollment = Enrollment.builder()
//                .id(99L)
//                .student(student)
//                .course(course)
//                .enrollmentDate(LocalDateTime.now())
//                .isActive(true)
//                .build();
//
//        EnrollmentResponse response = mapper.toResponse(enrollment, 10, 4, 40.0);
//
//        assertThat(response.totalTasks()).isEqualTo(10);
//        assertThat(response.totalCompletedTasks()).isEqualTo(4);
//        assertThat(response.completionPercentage()).isEqualTo(40.0);
//    }
//}
