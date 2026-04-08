package org.jas.ksinxapp.model;

import jakarta.persistence.*;

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

    public Modules() {
    }

    public Modules(Long id, String title, Integer sequenceOrder, String videoUrl, Course course) {
        this.id = id;
        this.title = title;
        this.sequenceOrder = sequenceOrder;
        this.videoUrl = videoUrl;
        this.course = course;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getSequenceOrder() {
        return sequenceOrder;
    }

    public void setSequenceOrder(Integer sequenceOrder) {
        this.sequenceOrder = sequenceOrder;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
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
