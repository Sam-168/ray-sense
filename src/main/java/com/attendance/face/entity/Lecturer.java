package com.attendance.face.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "lecturers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Lecturer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false, length = 100, name = "full_name")
    private String fullName;

    @Column(nullable = false, unique = true, length = 50, name = "employee_number")
    private String employeeNumber;

    @Column(length = 100)
    private String department;

    @Column(length = 20, name = "phone_number")
    private String phoneNumber;

    @Column(nullable = false, name = "is_active")
    private Boolean isActive = true;

    @Column(nullable = false, updatable = false, name = "created_at")
    private LocalDateTime createdAt;

    @Column(nullable = false, name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();

    }
}