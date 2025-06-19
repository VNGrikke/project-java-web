package java_web.service;

import java_web.entity.Course;
import java_web.repository.CourseRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseServiceImp implements CourseService {

    @Autowired
    private CourseRepo courseRepo;

    @Override
    public List<Course> findByStatusWithPaging(boolean status, int page, int size) {
        return courseRepo.findByStatusWithPaging(status, page, size);
    }

    @Override
    public Course getById(String id) {
        return courseRepo.getById(id);
    }

    @Override
    public boolean save(Course course) {
        return courseRepo.save(course);
    }

    @Override
    public boolean softDelete(String id) {
        return courseRepo.softDelete(id);
    }

    @Override
    public boolean restore(String id) {
        return courseRepo.restore(id);
    }

    @Override
    public int countByStatus(boolean status) {
        return courseRepo.countByStatus(status);
    }

    @Override
    public List<Course> searchByName(String keyword, boolean status, int page, int size, String sortField, boolean asc) {
        return courseRepo.searchByName(keyword, status, page, size, sortField, asc);
    }

    @Override
    public int countByName(String keyword, boolean status) {
        return courseRepo.countByName(keyword, status);
    }

    @Override
    public List<Course> sortCourses(String sortField, boolean asc, boolean status, int page, int size) {
        return courseRepo.sortCourses(sortField, asc, status, page, size);
    }

    @Override
    public List<Course> findAll() {
        return courseRepo.findAll();
    }

    @Override
    public int countByCourse(String courseId) {
        return courseRepo.countByCourse(courseId);
    }

    @Override
    public boolean existsByCourseId(String courseId) {
        return courseRepo.existsByCourseId(courseId);
    }

    @Override
    public boolean existsByCourseName(String courseName) {
        return courseRepo.existsByCourseName(courseName);
    }

}