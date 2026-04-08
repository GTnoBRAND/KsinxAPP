package org.jas.ksinxapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "submission")
public class TaskSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;
    @Column(nullable = false)
    private LocalDateTime submittedAt;

    @Column(nullable = false)
    private String fileUrl;

    private Integer score;

    @Column(columnDefinition = "TEXT")
    private String teacherFeedback;

    // Helper method to automatically set the submission time
    @PrePersist
    protected void onCreate(){
        this.submittedAt = LocalDateTime.now();
    }

    public TaskSubmission() {
    }

    public TaskSubmission(Long id, Task task, User student, LocalDateTime submittedAt, String fileUrl, Integer score, String teacherFeedback) {
        this.id = id;
        this.task = task;
        this.student = student;
        this.submittedAt = submittedAt;
        this.fileUrl = fileUrl;
        this.score = score;
        this.teacherFeedback = teacherFeedback;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public User getStudent() {
        return student;
    }

    public void setStudent(User student) {
        this.student = student;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getTeacherFeedback() {
        return teacherFeedback;
    }

    public void setTeacherFeedback(String teacherFeedback) {
        this.teacherFeedback = teacherFeedback;
    }

    @Override
    public String toString() {
        return "TaskSubmission{" +
                "id=" + id +
                ", task=" + task +
                ", student=" + student +
                ", submittedAt=" + submittedAt +
                ", fileUrl='" + fileUrl + '\'' +
                ", score=" + score +
                ", teacherFeedback='" + teacherFeedback + '\'' +
                '}';
    }
}
