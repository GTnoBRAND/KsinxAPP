package org.jas.ksinxapp.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "course_ratings",
        uniqueConstraints = @UniqueConstraint(name = "uk_course_ratings_user_course",
                                              columnNames = {"user_id", "course_id"}))
public class CourseRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(nullable = false)
    private Integer rating;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public CourseRating() {
    }

    public CourseRating(Long userId, Long courseId, Integer rating) {
        this.userId = userId;
        this.courseId = courseId;
        this.rating = rating;
    }
}
