package java_web.repository;

import java_web.dto.EnrollmentDTO;
import java_web.entity.Enrollment;
import java.util.List;


public interface EnrollmentRepo {
    Enrollment existsByStudentIdAndCourseId(Integer studentId, String courseId);
    List<String> findCourseIdsByStudentId(Integer studentId);
    void add(Enrollment enrollment);
    Enrollment getEnrollmentByIdAndStudentId(Integer id, Integer studentId);
    void updateEnrollment(Enrollment e);
    List<Enrollment> findByStudentIdWithPaging(Integer studentId, int page, int size, String sortField, boolean asc);
    List<Enrollment> searchByCourseNameAndStudentId(String keyword, Integer studentId, int page, int size, String sortField, boolean asc);
    int countByStudentId(Integer studentId);
    int countByCourseNameAndStudentId(String keyword, Integer studentId);
    int countAll();
    List<EnrollmentDTO> findEnrollmentDTOsWithDetails(String courseId, String keyword, String status, int page, int size, String sortField, boolean asc);
    int countEnrollmentDTOsWithDetails(String courseId, String keyword, String status);
    Enrollment getEnrollmentById(Integer id);
}