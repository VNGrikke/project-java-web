package java_web.service;

import java_web.entity.Student;
import java.util.List;

public interface StudentService {
    boolean register(Student student);
    Student login(String username, String password);
    Student findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    Student findById(Integer id);
    List<Student> findAll();
    List<Student> searchStudents(String keyword, int page, int pageSize, String sortField, boolean asc);
    int countByKeyword(String keyword);
    List<Student> sortStudents(String sortField, boolean asc, int page, int pageSize);
    int countAll();
    void toggleStatus(Integer id);
}