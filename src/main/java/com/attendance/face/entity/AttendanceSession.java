package com.attendance.face.entity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Which section this session is for
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private ModuleSection section;

    // Which lecturer started it
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private Lecturer createdBy;

    // Unique code e.g. "MAT101-A-20260509-0830"
    @Column(nullable = false, unique = true, name = "session_code")
    private String sessionCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private SessionStatus status = SessionStatus.ACTIVE;

    @Column(nullable = false, name = "started_at")
    private LocalDateTime startedAt;

    // Null until session is closed
    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    // Auto-close after this many minutes (60 min)
    @Column(nullable = false, name = "auto_close_minutes")
    private Integer autoCloseMinutes = 60;

    @Column(length = 255)
    private String notes;



    public boolean isActive() {
        return this.status == SessionStatus.ACTIVE;
    }


    public boolean shouldAutoClose() {
        if (this.status == SessionStatus.CLOSED) return false;
        LocalDateTime autoCloseTime = this.startedAt.plusMinutes(this.autoCloseMinutes);
        return LocalDateTime.now().isAfter(autoCloseTime);
    }
}
