package com.attendance.face.service;

import com.attendance.face.dto.LoginRequest;
import com.attendance.face.dto.StudentCreateRequest;
import com.attendance.face.dto.response.LoginResponse;
import com.attendance.face.entity.*;
import com.attendance.face.exception.DuplicateStudentException;
import com.attendance.face.exception.InvalidCredentialsException;
import com.attendance.face.exception.DuplicateStudentException;
import com.attendance.face.repository.AdminRepository;
import com.attendance.face.repository.LecturerRepository;
import com.attendance.face.repository.StudentRepository;
import com.attendance.face.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final LecturerRepository lecturerRepository;
    private final AdminRepository adminRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Autowired
    public AuthService(
            UserRepository userRepository,
            StudentRepository studentRepository,
            LecturerRepository lecturerRepository,
            AdminRepository adminRepository,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.lecturerRepository = lecturerRepository;
        this.adminRepository = adminRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.jwtService = jwtService;
    }

    /**
     * Register a new student
     */
    public LoginResponse registerStudent(StudentCreateRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateStudentException("Email already registered");
        }

        // Check if student number already exists
        if (studentRepository.existsByStudentNumber(request.getStudentNumber())) {
            throw new DuplicateStudentException("Student number already exists");
        }

        // Create User
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.STUDENT);
        user.setIsActive(true);

        User savedUser = userRepository.save(user);

        // Create Student
        Student student = new Student();
        student.setUserId(savedUser.getId());
        student.setFullName(request.getFullName());
        student.setStudentNumber(request.getStudentNumber());
        student.setClassId(request.getClassId());
        student.setIsActive(true);

        Student savedStudent = studentRepository.save(student);

        // Generate JWT token
        String token = jwtService.generateToken(savedUser);

        // Build response
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setRole(UserRole.STUDENT);
        response.setUserId(savedUser.getId());
        response.setEmail(savedUser.getEmail());
        response.setFullName(savedStudent.getFullName());
        response.setStudentId(savedStudent.getId());

        return response;
    }

    /**
     * Login (all users)
     */
    public LoginResponse login(LoginRequest request) {
        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        // Check if user is active
        if (!user.getIsActive()) {
            throw new InvalidCredentialsException("Account is deactivated");
        }

        // Generate JWT token
        String token = jwtService.generateToken(user);

        // Build response based on role
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setRole(user.getRole());
        response.setUserId(user.getId());
        response.setEmail(user.getEmail());

        // Populate role-specific data
        switch (user.getRole()) {
            case STUDENT:
                Student student = studentRepository.findByUserId(user.getId())
                        .orElseThrow(() -> new RuntimeException("Student record not found"));
                response.setFullName(student.getFullName());
                response.setStudentId(student.getId());
                break;

            case LECTURER:
                Lecturer lecturer = lecturerRepository.findByUserId(user.getId())
                        .orElseThrow(() -> new RuntimeException("Lecturer record not found"));
                response.setFullName(lecturer.getFullName());
                response.setLecturerId(lecturer.getId());
                break;

            case ADMIN:
                Admin admin = adminRepository.findByUserId(user.getId())
                        .orElseThrow(() -> new RuntimeException("Admin record not found"));
                response.setFullName(admin.getFullName());
                response.setAdminId(admin.getId());
                break;
        }

        return response;
    }
}
