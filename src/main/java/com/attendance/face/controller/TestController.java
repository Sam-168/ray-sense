package com.attendance.face.controller;

import com.attendance.face.entity.Attendance;
import com.attendance.face.entity.AttendanceStatus;
import com.attendance.face.entity.Student;
import com.attendance.face.repository.AttendanceRepository;
import com.attendance.face.repository.StudentRepository;
import com.attendance.face.service.AttendanceService;
import com.attendance.face.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private AttendanceService attendanceService;

    @PostMapping("/create-student")
    public Map<String, Object> createTestStudent(){
        Student student = studentService.registerStudent(
                "Tumelo Patja",
                "21121456",
                "3H"
        );

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Student created via Service");
        response.put("student", student);
        return response;

    }

//    @PostMapping("/mark-attendance/{studentId}")
//    public Map<String, Object> markAttendance(@PathVariable Long studentId){
//        Attendance attendance = attendanceService.markAttendance(
//                studentId,
//                "test-camera",
//                "test-session-" + System.currentTimeMillis()
//        );
//
//        Map<String, Object> response = new HashMap<>();
//        response.put("message", "attendance marked via service");
//        response.put("attendance", attendance);
//        return response;
//    }
    @GetMapping("/students")
    public List<Student> getAllStudents(){

        return studentService.getAllStudents();
    }
    @GetMapping("/attendance/today")
    public List<Attendance> getTodayAttendance() {
        return attendanceService.getTodayAttendance();
    }
    @GetMapping("/student/{id}/attendance-count")
    public Map<String, Object> getAttendanceCount(@PathVariable Long id){
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate today = LocalDate.now();

        long count = attendanceService.getAttendanceCount(id, startOfMonth, today);

        Map<String, Object> response = new HashMap<>();
        response.put("studentId", id);
        response.put("startDate" ,startOfMonth);
        response.put("endDate", today);
        response.put("attendanceCount", count);
        return response;
    }
}
