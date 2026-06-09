package org.jas.ksinxapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "users")
@Data
public class User{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,  unique = true)
    private String email;
    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    @Convert(converter = RoleConverter.class)
    private Role role;

    private String password;


    //a student can have many enrollments
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL)
    private List<Enrollment> enrollments;

    private boolean enabled;


    public enum  Role {
        ADMIN,
        STUDENT,
        TEACHER
    }


    public User() {
    }

    public User(Long id, String email, String fullName, Role role, String password , List<Enrollment> enrollments, boolean enabled) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.password = password;
        this.enrollments = enrollments;
        this.enabled = enabled;
    }
}
