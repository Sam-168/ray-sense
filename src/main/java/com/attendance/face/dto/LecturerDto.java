package com.attendance.face.dto;

import com.attendance.face.entity.Lecturer;
import com.attendance.face.entity.User;
import lombok.Data;

@Data
public class LecturerDto {

    private Long id;
    private String fullName;
    private String employeeNumber;
    private String department;
    private String phoneNumber;
    private String email;
    private Boolean isActive;
    private int sectionCount;

    public LecturerDto(Lecturer lecturer, User user, int sectionCount) {
        this.id             = lecturer.getId();
        this.fullName       = lecturer.getFullName();
        this.employeeNumber = lecturer.getEmployeeNumber();
        this.department     = lecturer.getDepartment();
        this.phoneNumber    = lecturer.getPhoneNumber();
        this.email          = user != null ? user.getEmail() : "";
        this.isActive       = lecturer.getIsActive();
        this.sectionCount   = sectionCount;
    }
}
