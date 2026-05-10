
package com.attendance.face.dto;

import com.attendance.face.entity.Module;
import lombok.Data;

@Data
public class ModuleDto {

    private Long id;
    private String moduleCode;
    private String moduleName;
    private String department;
    private Boolean isActive;
    private int sectionCount;

    public ModuleDto(Module module) {
        this.id          = module.getId();
        this.moduleCode  = module.getModuleCode();
        this.moduleName  = module.getModuleName();
        this.department  = module.getDepartment();
        this.isActive    = module.getIsActive();
        // getSections() is called once here inside a transaction - safe
        this.sectionCount = module.getSections() != null
                ? module.getSections().size() : 0;
    }
}