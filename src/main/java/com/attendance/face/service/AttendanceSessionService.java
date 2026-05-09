package com.attendance.face.service;
import com.attendance.face.entity.*;
import com.attendance.face.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Transactional
public class AttendanceSessionService {
    private final AttendanceSessionRepository sessionRepository;
    private final AttendanceRepository attendanceRepository;

    @Autowired
    public AttendanceSessionService(
            AttendanceSessionRepository sessionRepository,
            AttendanceRepository attendanceRepository) {
        this.sessionRepository = sessionRepository;
        this.attendanceRepository = attendanceRepository;
    }

    public AttendanceSession startSession(ModuleSection section, Lecturer lecturer, Integer autoCloseMinutes, String notes) {

        // Check if section already has an active session
        sessionRepository.findBySectionAndStatus(section, SessionStatus.ACTIVE)
                .ifPresent(existing -> {
                    throw new RuntimeException(
                            "Section " + section.getFullSectionName() +
                                    " already has an active session. Please end it first."
                    );
                });

        // Generate unique session code
        // Format: MAT101-A-20260509-0830
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm"));
        String sessionCode = section.getFullSectionName() + "-" + timestamp;

        // Create session
        AttendanceSession session = new AttendanceSession();
        session.setSection(section);
        session.setCreatedBy(lecturer);
        session.setSessionCode(sessionCode);
        session.setStatus(SessionStatus.ACTIVE);
        session.setStartedAt(LocalDateTime.now());
        session.setAutoCloseMinutes(autoCloseMinutes != null ? autoCloseMinutes : 60);
        session.setNotes(notes);

        return sessionRepository.save(session);
    }


    public AttendanceSession endSession(Long sessionId, Lecturer lecturer) {
        AttendanceSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        // Make sure this lecturer owns this session
        if (!session.getCreatedBy().getId().equals(lecturer.getId())) {
            throw new RuntimeException("You can only end your own sessions");
        }

        if (session.getStatus() == SessionStatus.CLOSED) {
            throw new RuntimeException("Session is already closed");
        }

        session.setStatus(SessionStatus.CLOSED);
        session.setEndedAt(LocalDateTime.now());

        return sessionRepository.save(session);
    }


    @Transactional(readOnly = true)
    public AttendanceSession getSession(Long sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
    }


    @Scheduled(fixedRate = 60000)
    public void autoCloseSessions() {
        List<AttendanceSession> activeSessions = sessionRepository
                .findByStatus(SessionStatus.ACTIVE);

        for (AttendanceSession session : activeSessions) {
            if (session.shouldAutoClose()) {
                session.setStatus(SessionStatus.CLOSED);
                session.setEndedAt(LocalDateTime.now());
                sessionRepository.save(session);
                System.out.println("Auto-closed session: " + session.getSessionCode());
            }
        }
    }
}
