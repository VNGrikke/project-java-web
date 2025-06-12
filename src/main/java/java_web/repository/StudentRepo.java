package java_web.repository;

import java_web.entity.Student;

public interface StudentRepo {
    boolean register(Student student);
    Student login(String username, String password);
    Student findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);

}
