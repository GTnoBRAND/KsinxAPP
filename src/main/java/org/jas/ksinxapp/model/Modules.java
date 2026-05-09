package org.jas.ksinxapp.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "modules")
public class Modules {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private Integer sequenceOrder;  //to keep lessons in the right order

    private String videoUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    private boolean isActive = true;

    public Modules() {
    }

    public Modules(Long id, String title, Integer sequenceOrder, String videoUrl, Course course) {
        this.id = id;
        this.title = title;
        this.sequenceOrder = sequenceOrder;
        this.videoUrl = videoUrl;
        this.course = course;
    }

    @Override
    public String toString() {
        return "Modules{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", sequenceOrder=" + sequenceOrder +
                ", videoUrl='" + videoUrl + '\'' +
                ", course=" + course +
                '}';
    }
}
