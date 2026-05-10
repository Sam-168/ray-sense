package com.attendance.face.controller;

import com.attendance.face.entity.*;
import com.attendance.face.entity.Module;
import com.attendance.face.repository.*;
import com.attendance.face.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
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
    public ResponseEntity<List<Map<String, Object>>> getAllModules() {
        List<Map<String, Object>> modules = moduleRepository.findAll()
                .stream().map(module -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", module.getId());
                    map.put("moduleCode", module.getModuleCode());
                    map.put("moduleName", module.getModuleName());
                    map.put("department", module.getDepartment());
                    map.put("isActive", module.getIsActive());
                    map.put("sectionCount", module.getSections().size());
                    return map;
                }).collect(Collectors.toList());
        return ResponseEntity.ok(modules);
    }

    /**
     * POST /api/admin/modules
     * Body: { moduleCode, moduleName, department }
     */
    @PostMapping("/modules")
    public ResponseEntity<Map<String, Object>> createModule(
            @RequestBody Map<String, String> body) {

        String moduleCode = body.get("moduleCode");
        String moduleName = body.get("moduleName");
        String department = body.get("department");

        if (moduleRepository.existsByModuleCode(moduleCode)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Module code already exists"));
        }

        Module module = new Module();
        module.setModuleCode(moduleCode.toUpperCase());
        module.setModuleName(moduleName);
        module.setDepartment(department);
        module.setIsActive(true);

        Module saved = moduleRepository.save(module);

        Map<String, Object> response = new HashMap<>();
        response.put("id", saved.getId());
        response.put("moduleCode", saved.getModuleCode());
        response.put("moduleName", saved.getModuleName());
        response.put("department", saved.getDepartment());
        response.put("message", "Module created successfully");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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
    public ResponseEntity<List<Map<String, Object>>> getAllSections() {
        List<Map<String, Object>> sections = sectionRepository.findAll()
                .stream().map(section -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", section.getId());
                    map.put("moduleCode", section.getModule().getModuleCode());
                    map.put("moduleName", section.getModule().getModuleName());
                    map.put("sectionCode", section.getSectionCode());
                    map.put("fullSectionName", section.getFullSectionName());
                    map.put("lecturerId", section.getLecturer().getId());
                    map.put("lecturerName", section.getLecturer().getFullName());
                    map.put("semester", section.getSemester());
                    map.put("year", section.getYear());
                    map.put("studentCount", section.getStudents().size());
                    map.put("isActive", section.getIsActive());
                    return map;
                }).collect(Collectors.toList());
        return ResponseEntity.ok(sections);
    }

    /**
     * POST /api/admin/sections
     * Body: { moduleId, lecturerId, sectionCode, semester, year }
     */
    @PostMapping("/sections")
    public ResponseEntity<Map<String, Object>> createSection(
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

        Map<String, Object> response = new HashMap<>();
        response.put("id", saved.getId());
        response.put("fullSectionName", saved.getFullSectionName());
        response.put("lecturerName", lecturer.getFullName());
        response.put("message", "Section created successfully");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Student already enrolled in this section"));
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
    public ResponseEntity<List<Map<String, Object>>> getAllLecturers() {
        List<Map<String, Object>> lecturers = lecturerRepository.findAll()
                .stream().map(lecturer -> {
                    // Get their user email
                    User user = userRepository.findById(lecturer.getUserId()).orElse(null);
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", lecturer.getId());
                    map.put("fullName", lecturer.getFullName());
                    map.put("employeeNumber", lecturer.getEmployeeNumber());
                    map.put("department", lecturer.getDepartment());
                    map.put("email", user != null ? user.getEmail() : "");
                    map.put("isActive", lecturer.getIsActive());
                    map.put("sectionCount",
                            sectionRepository.findByLecturerAndIsActive(lecturer, true).size()
                    );
                    return map;
                }).collect(Collectors.toList());
        return ResponseEntity.ok(lecturers);
    }

    /**
     * POST /api/admin/lecturers
     * Body: { email, password, fullName, employeeNumber, department, phoneNumber }
     */
    @PostMapping("/lecturers")
    public ResponseEntity<Map<String, Object>> createLecturer(
            @RequestBody Map<String, String> body) {

        String email          = body.get("email");
        String password       = body.get("password");
        String fullName       = body.get("fullName");
        String employeeNumber = body.get("employeeNumber");
        String department     = body.get("department");
        String phoneNumber    = body.get("phoneNumber");

        // Check duplicates
        if (userRepository.existsByEmail(email)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Email already exists"));
        }

        // Create user account
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(new BCryptPasswordEncoder().encode(password));
        user.setRole(UserRole.LECTURER);
        user.setIsActive(true);
        User savedUser = userRepository.save(user);

        // Create lecturer profile
        Lecturer lecturer = new Lecturer();
        lecturer.setUserId(savedUser.getId());
        lecturer.setFullName(fullName);
        lecturer.setEmployeeNumber(employeeNumber);
        lecturer.setDepartment(department);
        lecturer.setPhoneNumber(phoneNumber);
        lecturer.setIsActive(true);
        Lecturer savedLecturer = lecturerRepository.save(lecturer);

        Map<String, Object> response = new HashMap<>();
        response.put("id", savedLecturer.getId());
        response.put("fullName", savedLecturer.getFullName());
        response.put("email", email);
        response.put("message", "Lecturer account created successfully");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/admin/students
     */
    @GetMapping("/students")
    public ResponseEntity<List<Map<String, Object>>> getAllStudents() {
        List<Map<String, Object>> students = studentRepository.findAll()
                .stream().map(student -> {
                    User user = student.getUserId() != null ?
                            userRepository.findById(student.getUserId()).orElse(null) : null;
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", student.getId());
                    map.put("fullName", student.getFullName());
                    map.put("studentNumber", student.getStudentNumber());
                    map.put("email", user != null ? user.getEmail() : "");
                    map.put("isActive", student.getIsActive());
                    map.put("hasFaceEncoding", student.getFaceEncodingPath() != null);
                    map.put("enrolledSections",
                            studentSectionRepository.findByStudent(student).size()
                    );
                    return map;
                }).collect(Collectors.toList());
        return ResponseEntity.ok(students);
    }
}
