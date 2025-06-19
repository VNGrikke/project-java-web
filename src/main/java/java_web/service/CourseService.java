package java_web.service;

import java_web.entity.Course;
import java.util.List;

public interface CourseService {
    List<Course> findByStatusWithPaging(boolean status, int page, int size);
    Course getById(String id);
    boolean save(Course course);
    boolean softDelete(String id);
    boolean restore(String id);
    int countByStatus(boolean status);
    List<Course> searchByName(String keyword, boolean status, int page, int size, String sortField, boolean asc);
    int countByName(String keyword, boolean status);
    List<Course> sortCourses(String sortField, boolean asc, boolean status, int page, int size);
    List<Course> findAll();
    int countByCourse(String courseId);

    boolean existsByCourseId(String courseId);
    boolean existsByCourseName(String courseName);

}