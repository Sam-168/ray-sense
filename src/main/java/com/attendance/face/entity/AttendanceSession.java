package com.attendance.face.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_sessions")
@Data
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

    // Auto-close after this many minutes (default 60)
    @Column(nullable = false, name = "auto_close_minutes")
    private Integer autoCloseMinutes = 60;

    @Column(length = 255)
    private String notes;

    // ── Helpers ─────────────────────────────────────────────────────────────

    public boolean isActive() {
        return this.status == SessionStatus.ACTIVE;
    }

    /**
     * Check if session should be auto-closed
     * Called during polling to enforce the backup close
     */
    public boolean shouldAutoClose() {
        if (this.status == SessionStatus.CLOSED) return false;
        LocalDateTime autoCloseTime = this.startedAt.plusMinutes(this.autoCloseMinutes);
        return LocalDateTime.now().isAfter(autoCloseTime);
    }
}
