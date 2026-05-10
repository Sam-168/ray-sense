package com.attendance.face.dto.response;

import com.attendance.face.entity.ModuleSection;
import lombok.Data;

@Data
public class SectionDto {

    private Long id;
    private String moduleCode;
    private String moduleName;
    private String sectionCode;
    private String fullSectionName;
    private Long lecturerId;
    private String lecturerName;
    private String semester;
    private Integer year;
    private int studentCount;
    private Boolean isActive;
    private Long activeSessionId;
    private Boolean hasActiveSession;

    public SectionDto(ModuleSection section) {
        this.id              = section.getId();
        this.moduleCode      = section.getModule().getModuleCode();
        this.moduleName      = section.getModule().getModuleName();
        this.sectionCode     = section.getSectionCode();
        this.fullSectionName = section.getFullSectionName();
        this.lecturerId      = section.getLecturer().getId();
        this.lecturerName    = section.getLecturer().getFullName();
        this.semester        = section.getSemester();
        this.year            = section.getYear();
        this.isActive        = section.getIsActive();
        // Safe: called once inside transaction
        this.studentCount    = section.getStudents() != null
                ? section.getStudents().size() : 0;
    }
}
