package org.jas.ksinxapp.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "users")
@Component
public class User{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,  unique = true)
    private String email;
    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    private String password;


    //a student can have many enrollments
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL)
    private List<Enrollment> enrollments;


    public enum  Role {
        ADMIN,
        STUDENT,
        TEACHER
    }


    public User() {
    }

    public User(Long id, String email, String fullName, Role role, String password , List<Enrollment> enrollments) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.password = password;
        this.enrollments = enrollments;
    }


    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", role=" + role +
                ", password=" + password +
                ", enrollments=" + enrollments +
                '}';
    }
}
