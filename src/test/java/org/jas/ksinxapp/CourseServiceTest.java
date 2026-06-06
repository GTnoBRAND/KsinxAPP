package org.jas.ksinxapp;

import org.jas.ksinxapp.dtos.CourseProgressResponse;
import org.jas.ksinxapp.model.Course;
import org.jas.ksinxapp.model.Task;
import org.jas.ksinxapp.repo.CourseRepo;
import org.jas.ksinxapp.repo.EnrollmentRepo;
import org.jas.ksinxapp.repo.TaskSubmissionRepo;
import org.jas.ksinxapp.service.CourseService;
import org.jas.ksinxapp.service.EnrollmentService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

@ExtendWith(MockitoExtension.class)
public class CourseServiceTest {

    @Mock
    CourseRepo courseRepo;
    @Mock
    EnrollmentRepo enrollmentRepo;
    @Mock
    TaskSubmissionRepo taskSubmissionRepo;

    @InjectMocks
    EnrollmentService enrollmentService;

    @BeforeEach
    void setUp(){
        // Set fake authentication into the REAL SecurityContextHolder
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "sensei", null, List.of(new SimpleGrantedAuthority("ROLE_STUDENT"))
        );
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }

    @AfterEach
    void cleanUp(){
        SecurityContextHolder.clearContext();
    }

}
