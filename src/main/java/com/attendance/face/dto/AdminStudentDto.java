package com.attendance.face.dto.response;

import com.attendance.face.entity.Student;
import com.attendance.face.entity.User;
import lombok.Data;

@Data
public class AdminStudentDto {

    private Long id;
    private String fullName;
    private String studentNumber;
    private String email;
    private Boolean isActive;
    private Boolean hasFaceEncoding;
    private int enrolledSections;

    public AdminStudentDto(Student student, User user, int enrolledSections) {
        this.id              = student.getId();
        this.fullName        = student.getFullName();
        this.studentNumber   = student.getStudentNumber();
        this.email           = user != null ? user.getEmail() : "";
        this.isActive        = student.getIsActive();
        this.hasFaceEncoding = student.getFaceEncodingPath() != null;
        this.enrolledSections = enrolledSections;
    }
}