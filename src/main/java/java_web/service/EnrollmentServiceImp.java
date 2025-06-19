package java_web.service;

import java_web.entity.Enrollment;
import java_web.repository.EnrollmentRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EnrollmentServiceImp implements EnrollmentService {

    private final EnrollmentRepo enrollmentRepo;

    @Autowired
    public EnrollmentServiceImp(EnrollmentRepo enrollmentRepo) {
        this.enrollmentRepo = enrollmentRepo;
    }

    @Override
    public List<String> getEnrolledCourseIds(Integer studentId) {
        return enrollmentRepo.findCourseIdsByStudentId(studentId);
    }

    @Override
    public Enrollment existsByStudentAndCourse(Integer studentId, String courseId) {
        return enrollmentRepo.existsByStudentIdAndCourseId(studentId, courseId);
    }

    @Override
    public void add(Enrollment enrollment) {
        enrollmentRepo.add(enrollment);
    }

    @Override
    public Enrollment getEnrollmentByIdAndStudentId(Integer id, Integer studentId) {
        return enrollmentRepo.getEnrollmentByIdAndStudentId(id, studentId);
    }

    @Override
    public void updateEnrollment(Enrollment e) {
        enrollmentRepo.updateEnrollment(e);
    }

    @Override
    public List<Enrollment> findByStudentIdWithPaging(Integer studentId, int page, int size, String sortField, boolean asc) {
        return enrollmentRepo.findByStudentIdWithPaging(studentId, page, size, sortField, asc);
    }

    @Override
    public List<Enrollment> searchByCourseNameAndStudentId(String keyword, Integer studentId, int page, int size, String sortField, boolean asc) {
        return enrollmentRepo.searchByCourseNameAndStudentId(keyword, studentId, page, size, sortField, asc);
    }

    @Override
    public int countByStudentId(Integer studentId) {
        return enrollmentRepo.countByStudentId(studentId);
    }

    @Override
    public int countByCourseNameAndStudentId(String keyword, Integer studentId) {
        return enrollmentRepo.countByCourseNameAndStudentId(keyword, studentId);
    }

    @Override
    public int countAll() {
        return enrollmentRepo.countAll();
    }
}