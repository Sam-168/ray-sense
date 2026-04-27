package com.attendance.face.repository;

import com.attendance.face.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByUserId(Long userId);

    Optional<Student> findByStudentNumber(String studentNumber);

    List<Student> findByClassId(String classId);


    List<Student> findByIsActive(Boolean isActive);


     List<Student> findByClassIdAndIsActive(String classId, boolean isActive);


    boolean existsByStudentNumber(String studentNumber);


}
