package com.attendance.face.repository;

import com.attendance.face.entity.Student;
import com.attendance.face.entity.StudentSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentSectionRepository extends JpaRepository<StudentSection, Long> {
    List<StudentSection> findByStudent(Student student);

    Optional<StudentSection> findByStudentIdAndSectionId(Long studentId, Long sectionId);
}
