package org.jas.ksinxapp.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "task")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String title;
    @Column(columnDefinition = "TEXT")
    private String instructions;
    private Integer maxScore;
    private LocalDateTime dueDate;

    //a task belongs to a specific module in the course
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", nullable = false)
    private Modules module;


    public Task() {
    }

    public Task(Long id, String title, String instructions, Integer maxScore, LocalDateTime dueDate, Modules module) {
        this.id = id;
        this.title = title;
        this.instructions = instructions;
        this.maxScore = maxScore;
        this.dueDate = dueDate;
        this.module = module;
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

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public Integer getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(Integer maxScore) {
        this.maxScore = maxScore;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public Modules getModule() {
        return module;
    }

    public void setModule(Modules module) {
        this.module = module;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", instructions='" + instructions + '\'' +
                ", maxScore=" + maxScore +
                ", dueDate=" + dueDate +
                ", module=" + module +
                '}';
    }
}
