package org.jas.ksinxapp.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String title;
    @Column(columnDefinition = "TEXT")
    private String description;
    @Column(nullable = false)
    private BigDecimal price;

    //cover photo shown on the course card
    private String imageUrl;

    //short teaser/spoiler video
    private String videoUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CourseCategory category = CourseCategory.OTHER;

    //a course has many curriculums, modules
    @OneToMany(mappedBy = "course",cascade = CascadeType.ALL)
    private List<Modules> modules;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    private List<Enrollment> enrollments;

    @UpdateTimestamp
    private Instant updatedAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private Boolean isActive = true;

    public Course() {
    }

    public Course(Long id, String title, String description, BigDecimal price, List<Modules> modules, Boolean isActive) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.price = price;
        this.modules = modules;
        this.isActive = isActive;
    }

    @Override
    public String toString() {
        return "Course{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", category=" + category +
                ", modules=" + modules +
                ", isActive=" + isActive +
                '}';
    }
}
