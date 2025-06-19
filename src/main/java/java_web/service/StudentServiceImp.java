package java_web.service;

import java_web.entity.Student;
import java_web.repository.StudentRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentServiceImp implements StudentService {

    @Autowired
    private StudentRepo studentRepo;

    @Override
    public boolean register(Student student) {
        if (studentRepo.existsByUsername(student.getUsername())) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại");
        }
        if (studentRepo.existsByEmail(student.getEmail())) {
            throw new IllegalArgumentException("Email đã tồn tại");
        }
        if (student.getPhone() != null && !student.getPhone().isEmpty() && studentRepo.existsByPhone(student.getPhone())) {
            throw new IllegalArgumentException("Số điện thoại đã tồn tại");
        }
        return studentRepo.register(student);
    }

    @Override
    public Student login(String username, String password) {
        return studentRepo.login(username, password);
    }

    @Override
    public Student findByUsername(String username) {
        return studentRepo.findByUsername(username);
    }

    @Override
    public boolean existsByUsername(String username) {
        return studentRepo.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return studentRepo.existsByEmail(email);
    }

    @Override
    public boolean existsByPhone(String phone) {
        return studentRepo.existsByPhone(phone);
    }

    @Override
    public Student findById(Integer id) {
        return studentRepo.findById(id);
    }

    @Override
    public List<Student> findAll() {
        return studentRepo.findAll();
    }

    @Override
    public List<Student> searchStudents(String keyword, int page, int pageSize, String sortField, boolean asc) {
        return studentRepo.searchStudents(keyword, page, pageSize, sortField, asc);
    }

    @Override
    public int countByKeyword(String keyword) {
        return studentRepo.countByKeyword(keyword);
    }

    @Override
    public List<Student> sortStudents(String sortField, boolean asc, int page, int pageSize) {
        return studentRepo.sortStudents(sortField, asc, page, pageSize);
    }

    @Override
    public int countAll() {
        return studentRepo.countAll();
    }

    @Override
    public void toggleStatus(Integer id) {
        studentRepo.toggleStatus(id);
    }
}