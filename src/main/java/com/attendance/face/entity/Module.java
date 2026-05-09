package com.attendance.face.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "modules")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Module {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // e.g. "MAT101"
    @Column(nullable = false, unique = true, length = 20, name = "module_code")
    private String moduleCode;

    // e.g. "Mathematics 101"
    @Column(nullable = false, length = 200, name = "module_name")
    private String moduleName;

    @Column(length = 100)
    private String department;

    @Column(nullable = false, name = "is_active")
    private Boolean isActive = true;

    @OneToMany(mappedBy = "module", fetch = FetchType.LAZY)
    private Set<ModuleSection> sections = new HashSet<>();

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
}
