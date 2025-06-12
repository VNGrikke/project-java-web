package java_web.service;

import java_web.entity.Student;

public interface StudentService {
    boolean register(Student student);
    Student login(String username, String password);
    boolean isUnique(Student student);

}
