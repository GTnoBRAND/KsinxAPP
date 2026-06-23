package org.jas.ksinxapp.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jas.ksinxapp.dtos.RatingResponse;
import org.jas.ksinxapp.model.CourseRating;
import org.jas.ksinxapp.repo.CourseRatingRepo;
import org.jas.ksinxapp.repo.CourseRepo;
import org.jas.ksinxapp.repo.EnrollmentRepo;
import org.jas.ksinxapp.security.UserPrincipal;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CourseRatingService {

    private final CourseRatingRepo ratingRepo;
    private final CourseRepo courseRepo;
    private final EnrollmentRepo enrollmentRepo;

    @CacheEvict(value = "course", key = "#courseId")
    @Transactional
    public RatingResponse rateCourse(Long courseId, Integer rating) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rating must be between 1 and 5");
        }

        Long userId = currentUserId();

        if (!courseRepo.existsById(courseId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found");
        }

        if (!enrollmentRepo.existsByStudentIdAndCourseIdAndIsActiveTrue(userId, courseId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only enrolled students can rate this course");
        }

        CourseRating existing = ratingRepo.findByCourseIdAndUserId(courseId, userId)
                .orElseGet(() -> new CourseRating(userId, courseId, rating));
        existing.setRating(rating);
        ratingRepo.save(existing);

        return summary(courseId, userId);
    }

    @Transactional
    public RatingResponse getRating(Long courseId) {
        if (!courseRepo.existsById(courseId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found");
        }
        Long userId = currentUserIdOrNull();
        return summary(courseId, userId);
    }

    private RatingResponse summary(Long courseId, Long userId) {
        Double avg = ratingRepo.findAverageByCourseId(courseId);
        Long count = ratingRepo.countByCourseId(courseId);
        Integer mine = null;
        if (userId != null) {
            mine = ratingRepo.findByCourseIdAndUserId(courseId, userId)
                    .map(CourseRating::getRating)
                    .orElse(null);
        }
        double rounded = avg == null ? 0.0 : Math.round(avg * 10.0) / 10.0;
        return new RatingResponse(courseId, rounded, count, mine);
    }

    private Long currentUserId() {
        Long id = currentUserIdOrNull();
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return id;
    }

    private Long currentUserIdOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof UserPrincipal up)) {
            return null;
        }
        return up.getUser().getId();
    }
}
