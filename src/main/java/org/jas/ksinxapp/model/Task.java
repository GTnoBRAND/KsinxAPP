package org.jas.ksinxapp.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


@Setter
@Getter
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

    @PrePersist
    public void onCreate(){
        this.dueDate = LocalDateTime.now();
    }


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
