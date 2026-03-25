package com.attendance.face.service;

import com.attendance.face.entity.Student;
import com.attendance.face.exception.DuplicateStudentException;
import com.attendance.face.exception.StudentNotFoundException;
import com.attendance.face.repository.StudentRepository;
import com.sun.jdi.request.DuplicateRequestException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class StudentService {

    private final StudentRepository studentRepository;

    @Autowired
    public StudentService(StudentRepository studentRepository){
        this.studentRepository = studentRepository;
    }

    public Student registerStudent(String fullName, String studentNumber, String classId){
        //check if studentNumber exists
        if (studentRepository.existsByStudentNumber(studentNumber)){
            throw new DuplicateStudentException("Student number" + studentNumber + " already exists");
        }
        Student student = new Student();
        student.setFullName(fullName);
        student.setStudentNumber(studentNumber);
        student.setClassId(classId);
        student.setIsActive(true);

        return studentRepository.save(student);
    }

    public Student getStudentById(Long id){
        return studentRepository.findById(id)
                .orElseThrow(() -> new  StudentNotFoundException("Student with ID " + id + " not found"));
    }
    public Student getStudentByNumber(String studentNumber){
        return studentRepository.findByStudentNumber(studentNumber)
                .orElseThrow(() -> new StudentNotFoundException("Student number " + studentNumber + " not found"));
    }
    public List<Student> getAllStudents(){
        return studentRepository.findAll();
    }

    public List<Student> getActiveStudents(){
        return studentRepository.findByIsActive(true);
    }

    public List<Student> getStudentsByClass(String classId){
        return studentRepository.findByClassId(classId);
    }

    public List<Student> getActiveStudentsByClass(String classId){
        return studentRepository.findByClassIdAndIsActive(classId, true);
    }

    public Student updateStudent(Long id, String fullName, String classId){
        Student student = getStudentById(id);
        student.setFullName(fullName);
        student.setClassId(classId);

        return studentRepository.save(student);
    }
    //could cause problems because StudentId doesn't exist yet
    public Student updateFaceEncoding(Long studentId, String encodingPath){
        Student student = getStudentById(studentId);
        student.setFaceEncodingPath(encodingPath);
        return studentRepository.save(student);
    }

    public Student deactivateStudent(Long id){
        Student student = getStudentById(id);
        student.setIsActive(false);
        return studentRepository.save(student);
    }

    public Student activateStudent(Long id){

        Student student = getStudentById(id);
        student.setIsActive(true);
        return studentRepository.save(student);
    }

    public void deleteStudent(Long id){
        Student student = getStudentById(id);
        studentRepository.delete(student);
    }

    public boolean isStudentNumber(String studentNumber){
        return !studentRepository.existsByStudentNumber(studentNumber);
    }


}
