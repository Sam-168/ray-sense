package com.attendance.face.controller;

import com.attendance.face.entity.*;
import com.attendance.face.repository.*;
import com.attendance.face.service.AttendanceSessionService;
import com.attendance.face.service.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
@RestController
@RequestMapping("/api/lecturer")
public class LecturerController {
    private final JwtService jwtService;
    private final LecturerRepository lecturerRepository;
    private final ModuleSectionRepository sectionRepository;
    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final AttendanceSessionService sessionService;

    @Autowired
    public LecturerController(
            JwtService jwtService,
            LecturerRepository lecturerRepository,
            ModuleSectionRepository sectionRepository,
            AttendanceRepository attendanceRepository,
            StudentRepository studentRepository,
            UserRepository userRepository,
            AttendanceSessionService sessionService) {
        this.jwtService = jwtService;
        this.lecturerRepository = lecturerRepository;
        this.sectionRepository = sectionRepository;
        this.attendanceRepository = attendanceRepository;
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.sessionService = sessionService;
    }

    // ── Helper: extract lecturer from token ──────────────────────────────────
    private Lecturer getLecturerFromToken(String authHeader) {
        String token = authHeader.substring(7);
        Long userId = jwtService.extractUserId(token);
        return lecturerRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Lecturer not found"));
    }


    @GetMapping("/sections")
    public ResponseEntity<List<Map<String, Object>>> getMySections(
            @RequestHeader("Authorization") String authHeader) {

        Lecturer lecturer = getLecturerFromToken(authHeader);

        List<ModuleSection> sections = sectionRepository
                .findByLecturerAndIsActive(lecturer, true);

        List<Map<String, Object>> response = sections.stream().map(section -> {
            // Count students in this section
            int studentCount = section.getStudents().size();

            // Count today's attendance
            long todayPresent = attendanceRepository
                    .countBySectionAndDate(section, LocalDate.now());

            Map<String, Object> map = new HashMap<>();
            map.put("sectionId", section.getId());
            map.put("moduleCode", section.getModule().getModuleCode());
            map.put("moduleName", section.getModule().getModuleName());
            map.put("sectionCode", section.getSectionCode());
            map.put("fullSectionName", section.getFullSectionName());
            map.put("semester", section.getSemester());
            map.put("year", section.getYear());
            map.put("studentCount", studentCount);
            map.put("todayPresent", todayPresent);
            map.put("department", section.getModule().getDepartment());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }


    @GetMapping("/sections/{sectionId}/attendance/today")
    public ResponseEntity<Map<String, Object>> getTodayAttendance(
            @PathVariable Long sectionId,
            @RequestHeader("Authorization") String authHeader) {

        Lecturer lecturer = getLecturerFromToken(authHeader);
        ModuleSection section = getSectionAndVerifyOwnership(sectionId, lecturer);

        return ResponseEntity.ok(buildAttendanceResponse(section, LocalDate.now()));
    }


    @GetMapping("/sections/{sectionId}/attendance")
    public ResponseEntity<Map<String, Object>> getAttendanceByRange(
            @PathVariable Long sectionId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestHeader("Authorization") String authHeader) {

        Lecturer lecturer = getLecturerFromToken(authHeader);
        ModuleSection section = getSectionAndVerifyOwnership(sectionId, lecturer);

        // Get all attendance records in range for this section
        List<Attendance> records = attendanceRepository
                .findBySectionAndDateBetween(section, startDate, endDate);

        // Group by date
        Map<LocalDate, List<Attendance>> byDate = records.stream()
                .collect(Collectors.groupingBy(Attendance::getDate));

        List<Map<String, Object>> dailySummary = byDate.entrySet().stream()
                .sorted(Map.Entry.<LocalDate, List<Attendance>>comparingByKey().reversed())
                .map(entry -> {
                    long present = entry.getValue().stream()
                            .filter(a -> a.getStatus() == AttendanceStatus.PRESENT)
                            .count();

                    Map<String, Object> day = new HashMap<>();
                    day.put("date", entry.getKey());
                    day.put("present", present);
                    day.put("total", section.getStudents().size());
                    day.put("percentage",
                            section.getStudents().isEmpty() ? 0 :
                                    Math.round(present * 100.0 / section.getStudents().size()));
                    return day;
                })
                .collect(Collectors.toList());

        // Overall stats
        long totalPresent = records.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.PRESENT)
                .count();

        int totalPossible = section.getStudents().size() * byDate.size();

        Map<String, Object> response = new HashMap<>();
        response.put("sectionId", section.getId());
        response.put("fullSectionName", section.getFullSectionName());
        response.put("startDate", startDate);
        response.put("endDate", endDate);
        response.put("overallPercentage",
                totalPossible == 0 ? 0 : Math.round(totalPresent * 100.0 / totalPossible));
        response.put("dailySummary", dailySummary);

        return ResponseEntity.ok(response);
    }


    @GetMapping("/sections/{sectionId}/students")
    public ResponseEntity<List<Map<String, Object>>> getSectionStudents(
            @PathVariable Long sectionId,
            @RequestHeader("Authorization") String authHeader) {

        Lecturer lecturer = getLecturerFromToken(authHeader);
        ModuleSection section = getSectionAndVerifyOwnership(sectionId, lecturer);

        List<Map<String, Object>> students = section.getStudents().stream()
                .map(student -> {
                    long totalPresent = attendanceRepository
                            .countByStudentAndSection(student, section);

                    Map<String, Object> map = new HashMap<>();
                    map.put("studentId", student.getId());
                    map.put("fullName", student.getFullName());
                    map.put("studentNumber", student.getStudentNumber());
                    map.put("totalPresent", totalPresent);
                    map.put("hasFaceEncoding", student.getFaceEncodingPath() != null);
                    return map;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(students);
    }


    private ModuleSection getSectionAndVerifyOwnership(Long sectionId, Lecturer lecturer) {
        ModuleSection section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new RuntimeException("Section not found"));

        // Make sure this section belongs to this lecturer
        if (!section.getLecturer().getId().equals(lecturer.getId())) {
            throw new RuntimeException("Access denied to this section");
        }

        return section;
    }

    private Map<String, Object> buildAttendanceResponse(ModuleSection section, LocalDate date) {
        List<Attendance> todayRecords = attendanceRepository
                .findBySectionAndDate(section, date);

        Set<Student> allStudents = section.getStudents();

        // Map student ID to their attendance record for today
        Map<Long, Attendance> attendanceMap = todayRecords.stream()
                .collect(Collectors.toMap(
                        a -> a.getStudent().getId(),
                        a -> a
                ));

        // Build per-student status
        List<Map<String, Object>> studentStatuses = allStudents.stream()
                .map(student -> {
                    Attendance record = attendanceMap.get(student.getId());
                    Map<String, Object> map = new HashMap<>();
                    map.put("studentId", student.getId());
                    map.put("fullName", student.getFullName());
                    map.put("studentNumber", student.getStudentNumber());
                    map.put("status", record != null ? record.getStatus() : "ABSENT");
                    map.put("time", record != null ? record.getTime() : null);
                    return map;
                })
                .sorted(Comparator.comparing(m -> (String) m.get("fullName")))
                .collect(Collectors.toList());

        long presentCount = studentStatuses.stream()
                .filter(m -> "PRESENT".equals(m.get("status")))
                .count();

        Map<String, Object> response = new HashMap<>();
        response.put("sectionId", section.getId());
        response.put("fullSectionName", section.getFullSectionName());
        response.put("moduleName", section.getModule().getModuleName());
        response.put("date", date);
        response.put("totalStudents", allStudents.size());
        response.put("presentCount", presentCount);
        response.put("absentCount", allStudents.size() - presentCount);
        response.put("attendancePercentage",
                allStudents.isEmpty() ? 0 :
                        Math.round(presentCount * 100.0 / allStudents.size()));
        response.put("students", studentStatuses);

        return response;
    }
    @PostMapping("/sections/{sectionId}/sessions/start")
    public ResponseEntity<Map<String, Object>> startSession(
            @PathVariable Long sectionId,
            @RequestBody(required = false) Map<String, Object> body,
            @RequestHeader("Authorization") String authHeader) {

        Lecturer lecturer = getLecturerFromToken(authHeader);
        ModuleSection section = getSectionAndVerifyOwnership(sectionId, lecturer);

        // Optional params from body
        Integer autoCloseMinutes = body != null ?
                (Integer) body.getOrDefault("autoCloseMinutes", 60) : 60;
        String notes = body != null ? (String) body.get("notes") : null;

        AttendanceSession session = sessionService.startSession(
                section, lecturer, autoCloseMinutes, notes);

        Map<String, Object> response = new HashMap<>();
        response.put("sessionId", session.getId());
        response.put("sessionCode", session.getSessionCode());
        response.put("status", session.getStatus());
        response.put("startedAt", session.getStartedAt());
        response.put("autoCloseMinutes", session.getAutoCloseMinutes());
        response.put("sectionName", section.getFullSectionName());
        response.put("moduleName", section.getModule().getModuleName());
        response.put("totalStudents", section.getStudents().size());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    @PutMapping("/sessions/{sessionId}/end")
    public ResponseEntity<Map<String, Object>> endSession(
            @PathVariable Long sessionId,
            @RequestHeader("Authorization") String authHeader) {

        Lecturer lecturer = getLecturerFromToken(authHeader);
        AttendanceSession session = sessionService.endSession(sessionId, lecturer);

        Map<String, Object> response = new HashMap<>();
        response.put("sessionId", session.getId());
        response.put("status", session.getStatus());
        response.put("endedAt", session.getEndedAt());
        response.put("message", "Session ended successfully");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/sessions/{sessionId}/live")
    public ResponseEntity<Map<String, Object>> getLiveStats(
            @PathVariable Long sessionId,
            @RequestHeader("Authorization") String authHeader) {

        Lecturer lecturer = getLecturerFromToken(authHeader);
        AttendanceSession session = sessionService.getSession(sessionId);

        // Verify ownership
        if (!session.getCreatedBy().getId().equals(lecturer.getId())) {
            return ResponseEntity.status(403).build();
        }

        // Get all attendance records for this session
        List<Attendance> records = attendanceRepository.findBySession(session);

        long presentCount = records.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.PRESENT)
                .count();

        int totalStudents = session.getSection().getStudents().size();

        // Per student status
        Map<Long, Attendance> attendanceMap = records.stream()
                .collect(Collectors.toMap(
                        a -> a.getStudent().getId(),
                        a -> a
                ));

        List<Map<String, Object>> studentStatuses = session.getSection().getStudents()
                .stream()
                .map(student -> {
                    Attendance record = attendanceMap.get(student.getId());
                    Map<String, Object> map = new HashMap<>();
                    map.put("studentId", student.getId());
                    map.put("fullName", student.getFullName());
                    map.put("studentNumber", student.getStudentNumber());
                    map.put("status", record != null ? record.getStatus() : "ABSENT");
                    map.put("time", record != null ? record.getTime() : null);
                    return map;
                })
                .sorted(Comparator.comparing(m -> (String) m.get("fullName")))
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("sessionId", session.getId());
        response.put("sessionCode", session.getSessionCode());
        response.put("status", session.getStatus());
        response.put("sectionName", session.getSection().getFullSectionName());
        response.put("moduleName", session.getSection().getModule().getModuleName());
        response.put("startedAt", session.getStartedAt());
        response.put("autoCloseMinutes", session.getAutoCloseMinutes());
        response.put("presentCount", presentCount);
        response.put("totalStudents", totalStudents);
        response.put("attendancePercentage",
                totalStudents == 0 ? 0 : Math.round(presentCount * 100.0 / totalStudents));
        response.put("students", studentStatuses);

        return ResponseEntity.ok(response);
    }
}
