package com.attendance.face.repository;

import com.attendance.face.entity.Lecturer;
import com.attendance.face.entity.ModuleSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModuleSectionRepository extends JpaRepository<ModuleSection, Long> {
    List<ModuleSection> findByLecturerAndIsActive(Lecturer lecturer, Boolean isActive);

    @Query("SELECT ms FROM ModuleSection ms WHERE ms.module.moduleCode = :code AND ms.sectionCode = :section")
    Optional<ModuleSection> findByModuleCodeAndSectionCode(
            @Param("code") String moduleCode,
            @Param("section") String sectionCode
    );
}
