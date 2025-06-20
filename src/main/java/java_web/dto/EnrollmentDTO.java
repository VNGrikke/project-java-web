package java_web.dto;

import java_web.entity.EnrollmentStatus;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
public class EnrollmentDTO {
    private Integer enrollmentId;
    private String courseId;
    private Integer studentId;
    private String courseName;
    private String studentName;
    private LocalDateTime registeredAt;
    private EnrollmentStatus status;

    public String getFormat(){
        return registeredAt.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
    }

    public EnrollmentDTO(Integer enrollmentId, String courseId, Integer studentId, String courseName,
                         String studentName, LocalDateTime registeredAt, EnrollmentStatus status) {
        this.enrollmentId = enrollmentId;
        this.courseId = courseId;
        this.studentId = studentId;
        this.courseName = courseName;
        this.studentName = studentName;
        this.registeredAt = registeredAt;
        this.status = status;
    }
}