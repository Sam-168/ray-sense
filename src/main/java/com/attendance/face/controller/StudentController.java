package com.attendance.face.controller;

import com.attendance.face.dto.StudentCreateRequest;
import com.attendance.face.dto.StudentResponse;
import com.attendance.face.dto.StudentUpdateRequest;
import com.attendance.face.entity.Student;
import com.attendance.face.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    @Autowired
    public StudentController(StudentService studentService){
        this.studentService = studentService;
    }

    @PostMapping
    public ResponseEntity<StudentResponse> createStudent(@Valid @RequestBody StudentCreateRequest request){
        Student student = studentService.registerStudent(
                request.getFullName(),
                request.getStudentNumber(),
                request.getClassId()
        );
        StudentResponse response = new StudentResponse(student);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
     }

     @GetMapping
    public ResponseEntity<List<StudentResponse>> getAllStudents(){
        List<Student> students = studentService.getAllStudents();

        List<StudentResponse> responses = students.stream()
                .map(StudentResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
     }

     @GetMapping("/{id}")
    public ResponseEntity<StudentResponse> getStudentById(@PathVariable Long id){
        Student student = studentService.getStudentById(id);
        StudentResponse response = new StudentResponse(student);
        return ResponseEntity.ok(response);
     }

     @GetMapping("/number/{studentNumber}")
    public ResponseEntity<StudentResponse> getStudentByNumber(@PathVariable String studentNumber){
        Student student = studentService.getStudentByNumber(studentNumber);
        StudentResponse response = new StudentResponse(student);
        return ResponseEntity.ok(response);
     }
     @GetMapping("/active")
    public ResponseEntity<List<StudentResponse>> getActiveStudents(){
        List<Student> students = studentService.getActiveStudents();

        List<StudentResponse> responses = students.stream()
                .map(StudentResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
     }

     @PutMapping("/{id}")
    public ResponseEntity<StudentResponse> updateStudent(
            @PathVariable Long id,
            @Valid @RequestBody StudentUpdateRequest request){
        Student student = studentService.updateStudent(
                id,
                request.getFullName(),
                request.getClassId()
        );
        StudentResponse response = new StudentResponse(student);
        return ResponseEntity.ok(response);
     }

     @PutMapping("/{id}/deactivate")
    public ResponseEntity<StudentResponse> deactivateStudent(@PathVariable Long id){
        Student student = studentService.deactivateStudent(id);
        StudentResponse response = new StudentResponse(student);
        return ResponseEntity.ok(response);
     }

     @PutMapping("/{id}/activate")
    public ResponseEntity<StudentResponse> activateStudent(@PathVariable Long id){
        Student student = studentService.activateStudent(id);
        StudentResponse response = new StudentResponse(student);
        return ResponseEntity.ok(response);
     }
     @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id){
        studentService.deleteStudent(id);
        return ResponseEntity.noContent().build();
     }
}
