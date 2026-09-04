package com.attendance.face.config;
import com.attendance.face.entity.*;
import com.attendance.face.repository.AdminRepository;
import com.attendance.face.repository.LecturerRepository;
import com.attendance.face.repository.StudentRepository;
import com.attendance.face.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@Profile({"dev", "test"})
public class DataSeeder {
    @Bean
    public CommandLineRunner seedData(
            UserRepository userRepository,
            LecturerRepository lecturerRepository,
            AdminRepository adminRepository,
            StudentRepository studentRepository) {

        return args -> {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

            // ===== Create Admin Account =====
            if (!userRepository.existsByEmail("admin@attendance.com")) {
                // Create user record
                User adminUser = new User();
                adminUser.setEmail("admin@attendance.com");
                adminUser.setPasswordHash(encoder.encode("admin123"));
                adminUser.setRole(UserRole.ADMIN);
                adminUser.setIsActive(true);
                User savedAdminUser = userRepository.save(adminUser);

                // Create admin profile
                Admin admin = new Admin();
                admin.setUserId(savedAdminUser.getId());
                admin.setFullName("System Administrator");
                admin.setPosition("System Administrator");
                admin.setDepartment("IT Department");
                adminRepository.save(admin);

                System.out.println("✅ Admin account created: admin@attendance.com / admin123");
            }

            // ===== Create Test Lecturer Account =====
            if (!userRepository.existsByEmail("lecturer@attendance.com")) {
                // Create user record
                User lecturerUser = new User();
                lecturerUser.setEmail("lecturer@attendance.com");
                lecturerUser.setPasswordHash(encoder.encode("lecturer123"));
                lecturerUser.setRole(UserRole.LECTURER);
                lecturerUser.setIsActive(true);
                User savedLecturerUser = userRepository.save(lecturerUser);

                // Create lecturer profile
                Lecturer lecturer = new Lecturer();
                lecturer.setUserId(savedLecturerUser.getId());
                lecturer.setFullName("Dr. John Smith");
                lecturer.setEmployeeNumber("EMP001");
                lecturer.setDepartment("Computer Science");
                lecturer.setPhoneNumber("+27 82 123 4567");
                lecturer.setIsActive(true);
                lecturerRepository.save(lecturer);

                System.out.println(" Lecturer account created: lecturer@attendance.com / lecturer123");
            }
            if (!userRepository.existsByEmail("john@student.com")) {
                // Create user record
                User studentUser = new User();
                studentUser.setEmail("john@student.com");
                studentUser.setPasswordHash(encoder.encode("password123"));
                studentUser.setRole(UserRole.STUDENT);
                studentUser.setIsActive(true);
                User savedStudentUser = userRepository.save(studentUser);

                // Create lecturer profile
                Student student = new Student();
                student.setUserId(savedStudentUser.getId());
                student.setFullName("John Ndlela");
                student.setStudentNumber("444888");
                student.setClassId("3H");
                student.setIsActive(true);
                studentRepository.save(student);

                System.out.println("student account created! / John Ndlela password123");
            }

            System.out.println(" Database seeding complete");
        };
    }
}
