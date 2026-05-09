package com.attendance.face.repository;

import com.attendance.face.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByStudent(Student student);

    List<Attendance> findByDate(LocalDate date);

    Optional<Attendance> findByStudentAndDate(Student student, LocalDate date);

    List<Attendance> findByDateBetween(LocalDate startDate, LocalDate endDate);

    List<Attendance> findBySessionId(String sessionId);

    List<Attendance> findByStatus(AttendanceStatus status);

    boolean existsByStudentAndDate(Student student, LocalDate date);

    //Custom query to find recent attendance
    @Query("SELECT a FROM Attendance a WHERE a.student = :student AND a.createdAt >= :since")
    List<Attendance> findRecentAttendanceByStudent(
            @Param("student") Student student,
            @Param("since") LocalDateTime since
    );

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.student =:student AND a.date BETWEEN :startDate AND :endDate")
    long countAttendanceByStudentAndDateRange(
            @Param("student") Student student,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.section = :section AND a.date = :date AND a.status = 'PRESENT'")
    long countBySectionAndDate(@Param("section") ModuleSection section, @Param("date") LocalDate date);

    List<Attendance> findBySectionAndDate(ModuleSection section, LocalDate date);

    List<Attendance> findBySectionAndDateBetween(ModuleSection section, LocalDate startDate, LocalDate endDate);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.student = :student AND a.section = :section AND a.status = 'PRESENT'")
    long countByStudentAndSection(@Param("student") Student student, @Param("section") ModuleSection section);

    boolean existsByStudentAndSession(Student student, AttendanceSession session);

    long countBySessionAndStatus(AttendanceSession session, AttendanceStatus status);

    List<Attendance> findBySession(AttendanceSession session);
}
