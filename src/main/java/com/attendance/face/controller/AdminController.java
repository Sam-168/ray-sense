package com.attendance.face.controller;

import com.attendance.face.dto.LecturerDto;
import com.attendance.face.dto.ModuleDto;
import com.attendance.face.dto.response.AdminStudentDto;
import com.attendance.face.dto.response.SectionDto;
import com.attendance.face.entity.*;
import com.attendance.face.entity.Module;
import com.attendance.face.repository.*;
import com.attendance.face.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@Transactional
public class AdminController {

    private final ModuleRepository moduleRepository;
    private final ModuleSectionRepository sectionRepository;
    private final StudentRepository studentRepository;
    private final StudentSectionRepository studentSectionRepository;
    private final LecturerRepository lecturerRepository;
    private final UserRepository userRepository;
    private final AttendanceRepository attendanceRepository;
    private final AttendanceSessionRepository sessionRepository;

    @Autowired
    public AdminController(
            ModuleRepository moduleRepository,
            ModuleSectionRepository sectionRepository,
            StudentRepository studentRepository,
            StudentSectionRepository studentSectionRepository,
            LecturerRepository lecturerRepository,
            UserRepository userRepository,
            AttendanceRepository attendanceRepository,
            AttendanceSessionRepository sessionRepository) {
        this.moduleRepository = moduleRepository;
        this.sectionRepository = sectionRepository;
        this.studentRepository = studentRepository;
        this.studentSectionRepository = studentSectionRepository;
        this.lecturerRepository = lecturerRepository;
        this.userRepository = userRepository;
        this.attendanceRepository = attendanceRepository;
        this.sessionRepository = sessionRepository;
    }


    /**
     * GET /api/admin/dashboard
     * System-wide statistics
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalStudents", studentRepository.count());
        stats.put("totalLecturers", lecturerRepository.count());
        stats.put("totalModules", moduleRepository.count());
        stats.put("totalSections", sectionRepository.count());
        stats.put("activeSessions", sessionRepository.findByStatus(SessionStatus.ACTIVE).size());
        stats.put("totalAttendanceRecords", attendanceRepository.count());
        return ResponseEntity.ok(stats);
    }


    /**
     * GET /api/admin/modules
     */
    @GetMapping("/modules")
    public ResponseEntity<List<ModuleDto>> getAllModules() {
        List<ModuleDto> modules = moduleRepository.findAll()
                .stream()
                .map(ModuleDto::new)        // ← DTO conversion inside transaction
                .collect(Collectors.toList());
        return ResponseEntity.ok(modules);
    }
    /**
     * POST /api/admin/modules
     * Body: { moduleCode, moduleName, department }
     */
    @PostMapping("/modules")
    public ResponseEntity<ModuleDto> createModule(@RequestBody Map<String, String> body) {
        String moduleCode = body.get("moduleCode");
        String moduleName = body.get("moduleName");
        String department = body.get("department");

        if (moduleRepository.existsByModuleCode(moduleCode)) {
            throw new RuntimeException("Module code already exists");
        }

        Module module = new Module();
        module.setModuleCode(moduleCode.toUpperCase());
        module.setModuleName(moduleName);
        module.setDepartment(department);
        module.setIsActive(true);

        Module saved = moduleRepository.save(module);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ModuleDto(saved));
    }

    /**
     * DELETE /api/admin/modules/{id}
     */
    @DeleteMapping("/modules/{id}")
    public ResponseEntity<Map<String, Object>> deleteModule(@PathVariable Long id) {
        Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Module not found"));
        module.setIsActive(false);
        moduleRepository.save(module);
        return ResponseEntity.ok(Map.of("message", "Module deactivated"));
    }

    /**
     * GET /api/admin/sections
     */
    @GetMapping("/sections")
    public ResponseEntity<List<SectionDto>> getAllSections() {
        List<SectionDto> sections = sectionRepository.findAll()
                .stream()
                .map(SectionDto::new)       // ← DTO conversion inside transaction
                .collect(Collectors.toList());
        return ResponseEntity.ok(sections);
    }

    /**
     * POST /api/admin/sections
     * Body: { moduleId, lecturerId, sectionCode, semester, year }
     */
    @PostMapping("/sections")
    public ResponseEntity<SectionDto> createSection(
            @RequestBody Map<String, Object> body) {

        Long moduleId   = Long.valueOf(body.get("moduleId").toString());
        Long lecturerId = Long.valueOf(body.get("lecturerId").toString());
        String sectionCode = body.get("sectionCode").toString().toUpperCase();
        String semester    = body.get("semester").toString();
        Integer year       = Integer.valueOf(body.get("year").toString());

        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module not found"));

        Lecturer lecturer = lecturerRepository.findById(lecturerId)
                .orElseThrow(() -> new RuntimeException("Lecturer not found"));

        ModuleSection section = new ModuleSection();
        section.setModule(module);
        section.setLecturer(lecturer);
        section.setSectionCode(sectionCode);
        section.setSemester(semester);
        section.setYear(year);
        section.setIsActive(true);

        ModuleSection saved = sectionRepository.save(section);
        return ResponseEntity.status(HttpStatus.CREATED).body(new SectionDto(saved));
    }

    /**
     * DELETE /api/admin/sections/{id}
     */
    @DeleteMapping("/sections/{id}")
    public ResponseEntity<Map<String, Object>> deleteSection(@PathVariable Long id) {
        ModuleSection section = sectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Section not found"));
        section.setIsActive(false);
        sectionRepository.save(section);
        return ResponseEntity.ok(Map.of("message", "Section deactivated"));
    }

    /**
     * GET /api/admin/sections/{sectionId}/students
     * All students enrolled in a section
     */
    @GetMapping("/sections/{sectionId}/students")
    public ResponseEntity<Map<String, Object>> getSectionStudents(
            @PathVariable Long sectionId) {

        ModuleSection section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new RuntimeException("Section not found"));

        // Enrolled students
        List<Map<String, Object>> enrolled = section.getStudents().stream()
                .map(student -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", student.getId());
                    map.put("fullName", student.getFullName());
                    map.put("studentNumber", student.getStudentNumber());
                    map.put("hasFaceEncoding", student.getFaceEncodingPath() != null);
                    return map;
                }).collect(Collectors.toList());

        // Students NOT enrolled (available to add)
        Set<Long> enrolledIds = section.getStudents().stream()
                .map(Student::getId).collect(Collectors.toSet());

        List<Map<String, Object>> available = studentRepository.findByIsActive(true)
                .stream()
                .filter(s -> !enrolledIds.contains(s.getId()))
                .map(student -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", student.getId());
                    map.put("fullName", student.getFullName());
                    map.put("studentNumber", student.getStudentNumber());
                    map.put("hasFaceEncoding", student.getFaceEncodingPath() != null);
                    return map;
                }).collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("sectionName", section.getFullSectionName());
        response.put("enrolled", enrolled);
        response.put("available", available);

        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/admin/sections/{sectionId}/enroll
     * Body: { studentId }
     */
    @PostMapping("/sections/{sectionId}/enroll")
    public ResponseEntity<Map<String, Object>> enrollStudent(
            @PathVariable Long sectionId,
            @RequestBody Map<String, Object> body) {

        Long studentId = Long.valueOf(body.get("studentId").toString());

        ModuleSection section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new RuntimeException("Section not found"));

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        // Check already enrolled
        boolean alreadyEnrolled = section.getStudents().stream()
                .anyMatch(s -> Objects.equals(s.getId(), studentId));

        if (alreadyEnrolled) {
            throw new RuntimeException("Student already enrolled in this section");
        }

        // Enroll
        StudentSection enrollment = new StudentSection();
        enrollment.setStudent(student);
        enrollment.setSection(section);
        enrollment.setEnrolledAt(LocalDateTime.now());
        studentSectionRepository.save(enrollment);

        return ResponseEntity.ok(Map.of(
                "message", student.getFullName() + " enrolled in " + section.getFullSectionName()
        ));
    }

    /**
     * DELETE /api/admin/sections/{sectionId}/students/{studentId}
     * Remove student from section
     */
    @DeleteMapping("/sections/{sectionId}/students/{studentId}")
    public ResponseEntity<Map<String, Object>> removeStudent(
            @PathVariable Long sectionId,
            @PathVariable Long studentId) {

        StudentSection enrollment = studentSectionRepository
                .findByStudentIdAndSectionId(studentId, sectionId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));

        studentSectionRepository.delete(enrollment);

        return ResponseEntity.ok(Map.of("message", "Student removed from section"));
    }

    /**
     * GET /api/admin/lecturers
     */
    @GetMapping("/lecturers")
    public ResponseEntity<List<LecturerDto>> getAllLecturers() {
        List<LecturerDto> lecturers = lecturerRepository.findAll()
                .stream()
                .map(lecturer -> {
                    User user = userRepository.findById(lecturer.getUserId()).orElse(null);
                    // Count sections safely
                    int sectionCount = sectionRepository
                            .findByLecturerAndIsActive(lecturer, true).size();
                    return new LecturerDto(lecturer, user, sectionCount);
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(lecturers);
    }

    /**
     * POST /api/admin/lecturers
     * Body: { email, password, fullName, employeeNumber, department, phoneNumber }
     */
    @PostMapping("/lecturers")
    public ResponseEntity<LecturerDto> createLecturer(@RequestBody Map<String, String> body) {
        String email          = body.get("email");
        String password       = body.get("password");
        String fullName       = body.get("fullName");
        String employeeNumber = body.get("employeeNumber");
        String department     = body.get("department");
        String phoneNumber    = body.get("phoneNumber");

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(new BCryptPasswordEncoder().encode(password));
        user.setRole(UserRole.LECTURER);
        user.setIsActive(true);
        User savedUser = userRepository.save(user);

        Lecturer lecturer = new Lecturer();
        lecturer.setUserId(savedUser.getId());
        lecturer.setFullName(fullName);
        lecturer.setEmployeeNumber(employeeNumber);
        lecturer.setDepartment(department);
        lecturer.setPhoneNumber(phoneNumber);
        lecturer.setIsActive(true);
        Lecturer savedLecturer = lecturerRepository.save(lecturer);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new LecturerDto(savedLecturer, savedUser, 0));
    }

    /**
     * GET /api/admin/students
     */
    @GetMapping("/students")
    public ResponseEntity<List<AdminStudentDto>> getAllStudents() {
        List<AdminStudentDto> students = studentRepository.findAll()
                .stream()
                .map(student -> {
                    User user = student.getUserId() != null
                            ? userRepository.findById(student.getUserId()).orElse(null)
                            : null;
                    int enrolledSections = studentSectionRepository
                            .findByStudent(student).size();
                    return new AdminStudentDto(student, user, enrolledSections);
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(students);
    }
}
