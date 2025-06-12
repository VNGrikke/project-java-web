package java_web.service;

import java_web.entity.Student;
import java_web.repository.StudentRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StudentServiceImp implements StudentService {

    @Autowired
    private StudentRepo studentRepo;

    @Override
    public boolean register(Student student) {
        isUnique(student);
        return studentRepo.register(student);
    }


    @Override
    public Student login(String username, String password) {
        return studentRepo.login(username, password);
    }

    @Override
    public boolean isUnique(Student student) {
        if (studentRepo.existsByUsername(student.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (studentRepo.existsByEmail(student.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        if (student.getPhone() != null && !student.getPhone().isEmpty()
                && studentRepo.existsByPhone(student.getPhone())) {
            throw new IllegalArgumentException("Phone already exists");
        }
        return true;
    }

}