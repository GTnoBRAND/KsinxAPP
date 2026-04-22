package org.jas.ksinxapp.model;

import jakarta.persistence.*;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "users")
@Component
public class User implements Serializable{

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
        USER        //you and any other teacher
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }



    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public List<Enrollment> getEnrollments() {
        return enrollments;
    }

    public void setEnrollments(List<Enrollment> enrollments) {
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
