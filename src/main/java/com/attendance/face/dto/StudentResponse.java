package com.attendance.face.dto;

import com.attendance.face.entity.Student;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentResponse {
    private Long id;
    private String fullName;
    private String studentNumber;
    private String classId;
    private Boolean isActive;
    private Boolean hasFaceEncoding;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public StudentResponse(Student student){
        this.id = student.getId();
        this.fullName = student.getFullName();
        this.studentNumber = student.getStudentNumber();
        this.classId = student.getClassId();
        this.isActive = student.getIsActive();
        this.hasFaceEncoding = student.getFaceEncodingPath() != null;
        this.createdAt = student.getCreatedAt();
        this.updatedAt = student.getUpdatedAt();

    }
}
