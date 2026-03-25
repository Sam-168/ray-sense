package com.attendance.face.service;

import com.attendance.face.entity.Attendance;
import com.attendance.face.entity.AttendanceStatus;
import com.attendance.face.entity.Student;
import com.attendance.face.exception.DuplicateAttendanceException;
import com.attendance.face.exception.StudentNotActiveException;
import com.attendance.face.repository.AttendanceRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@Transactional
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final StudentService studentService;

    private static final int DUPLICATE_PREVENTION_MINUTES = 5;

    @Autowired
    public AttendanceService(AttendanceRepository attendanceRepository, StudentService studentService){
        this.attendanceRepository = attendanceRepository;
        this.studentService = studentService;
    }
    public Attendance markAttendance(Long studentId, String captureSource, String sessionId){
        Student student = studentService.getStudentById(studentId);

        if (!student.getIsActive()){
            throw new StudentNotActiveException("Student " + student.getStudentNumber() + " is not active");
        }
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        if (attendanceRepository.existsByStudentAndDate(student, today)){
            throw new DuplicateAttendanceException(
                    "Student " + student.getStudentNumber() + " already marked attendance today"
            );
        }
        LocalDateTime since = LocalDateTime.now().minusMinutes(DUPLICATE_PREVENTION_MINUTES);
        List<Attendance> recentAttendance = attendanceRepository.findRecentAttendanceByStudent(student, since);

        if (!recentAttendance.isEmpty()){
            throw new DuplicateAttendanceException(
                    "Student scanned " + recentAttendance.size() + " times in last " +
                            DUPLICATE_PREVENTION_MINUTES + " minutes. Please wait. "
            );
        }
        Attendance attendance = new Attendance();
        attendance.setStudent(student);
        attendance.setDate(today);
        attendance.setTime(now);
        attendance.setStatus(AttendanceStatus.PRESENT);
        attendance.setCaptureSource(captureSource);
        attendance.setSessionId(sessionId);

        return attendanceRepository.save(attendance);

    }
    public List<Attendance> getAttendanceByStudent(Long studentId){
        Student student = studentService.getStudentById(studentId);
        return attendanceRepository.findByStudent(student);
    }
    public List<Attendance> getAttendanceByDate(LocalDate date){
        return attendanceRepository.findByDate(date);
    }


    public List<Attendance> getTodayAttendance(){
        return attendanceRepository.findByDate(LocalDate.now());
    }
    public List<Attendance> getAttendanceByDateRange(LocalDate startDate, LocalDate endDate){
        return attendanceRepository.findByDateBetween(startDate, endDate);
    }

    public List<Attendance> getAttendanceBySession(String sessionId){
        return attendanceRepository.findBySessionId(sessionId);
    }
    public boolean hasAttended(Long studentId, LocalDate date){
        Student student = studentService.getStudentById(studentId);
        return attendanceRepository.existsByStudentAndDate(student, date);
    }

    public long getAttendanceCount(Long studentId, LocalDate startDate, LocalDate endDate){
        Student student = studentService.getStudentById(studentId);
        return attendanceRepository.countAttendanceByStudentAndDateRange(student, startDate, endDate);
    }
    public double calculateAttendancePercentage(Long studentId, LocalDate startDate, LocalDate endDate, int totalPossibleDays){
        long daysPresent = getAttendanceCount(studentId, startDate, endDate);

        if (totalPossibleDays == 0){
            return 0.0;
        }
        return (daysPresent * 100.0) / totalPossibleDays;
    }
}
