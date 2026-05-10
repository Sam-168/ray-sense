package com.attendance.face.repository;

import com.attendance.face.entity.AttendanceSession;
import com.attendance.face.entity.ModuleSection;
import com.attendance.face.entity.SessionStatus;
import com.attendance.face.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, Long> {
    // Check if a section already has an active session
    Optional<AttendanceSession> findBySectionAndStatus(ModuleSection section, SessionStatus status);

    // Get all active sessions (used for auto-close check)
    List<AttendanceSession> findByStatus(SessionStatus status);

    // Get all active sessions for sections a student is enrolled in
    @Query("""
        SELECT s FROM AttendanceSession s
        WHERE s.status = 'ACTIVE'
        AND s.section IN (
            SELECT ss.section FROM StudentSection ss
            WHERE ss.student = :student
        )
    """)
    List<AttendanceSession> findActiveSessionsForStudent(@Param("student") Student student);

    // Get all sessions for a section (history)
    List<AttendanceSession> findBySectionOrderByStartedAtDesc(ModuleSection section);
}
