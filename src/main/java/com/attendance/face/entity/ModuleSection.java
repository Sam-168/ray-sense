package com.attendance.face.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "module_sections")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModuleSection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Which module this section belongs to
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", nullable = false)
    private Module module;

    // Which lecturer teaches this section
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecturer_id", nullable = false)
    private Lecturer lecturer;

    // e.g. "A", "B", "C"
    @Column(nullable = false, length = 10, name = "section_code")
    private String sectionCode;

    // e.g. "Semester 1"
    @Column(length = 20)
    private String semester;

    @Column
    private Integer year;

    @Column(nullable = false, name = "is_active")
    private Boolean isActive = true;

    // Students enrolled in this section
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "student_sections",
            joinColumns = @JoinColumn(name = "section_id"),
            inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    private Set<Student> students = new HashSet<>();

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
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }

    // Helper: returns full section name e.g. "MAT101-A"
    public String getFullSectionName() {
        return module.getModuleCode() + "-" + sectionCode;
    }
}
