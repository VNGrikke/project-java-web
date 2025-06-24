package java_web.controller;

import java_web.dto.StudentDTO;
import java_web.dto.PasswordDTO;
import java_web.entity.Course;
import java_web.entity.Enrollment;
import java_web.entity.EnrollmentStatus;
import java_web.entity.Student;
import java_web.service.CourseService;
import java_web.service.EnrollmentService;
import java_web.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private HttpSession session;

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private StudentService studentService;

    // Kiểm tra vai trò sinh viên
    private boolean checkStudentRole(HttpSession session, String username) {
        Student student = (Student) session.getAttribute("user");
        return student != null && student.getUsername().equals(username) && !student.getRole();
    }


    // Xem danh sách + tìm kiếm khóa học
    @GetMapping
    public String home(@RequestParam(defaultValue = "1") int page,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) String registerCourseId,
                       Model model,
                       HttpSession session,
                       @CookieValue(value = "username", defaultValue = "") String username) {

        if (username.isEmpty() || !checkStudentRole(session, username)) {
            return "redirect:/login";
        }

        int size = 5;
        List<Course> courses;
        int totalCourses;

        if (keyword != null && !keyword.trim().isEmpty()) {
            courses = courseService.searchByName(keyword.trim(), true, page, size, "name", true);
            totalCourses = courseService.countByName(keyword.trim(), true);
        } else {
            courses = courseService.findByStatusWithPaging(true, page, size);
            totalCourses = courseService.countByStatus(true);
        }

        int totalPages = (int) Math.ceil((double) totalCourses / size);

        Student student = (Student) session.getAttribute("user");
        List<String> enrolledCourseIds = enrollmentService.getEnrolledCourseIds(student.getId());

        for (Course c : courses) {
            c.setRegistered(enrolledCourseIds.contains(c.getId()));
        }

        model.addAttribute("courses", courses);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("keyword", keyword);

        // Xử lý hiển thị modal nếu có registerCourseId
        if (registerCourseId != null) {
            Course course = courseService.getById(registerCourseId);
            Enrollment enrollment = enrollmentService.existsByStudentAndCourse(student.getId(), registerCourseId);
            if (course != null && course.getStatus() && enrollment == null) {
                model.addAttribute("isRegistry", true);
                model.addAttribute("course", course);
            } else {
                return "redirect:/student?page=" + page + (keyword != null ? "&keyword=" + keyword : "") + "&error=invalid";
            }
        }

        return "student/home";
    }

    // Hiển thị modal xác nhận đăng ký khóa học
    @GetMapping("/register/{id}")
    public String showRegisterConfirm(@PathVariable("id") String id,
                                      @RequestParam(defaultValue = "1") int page,
                                      @RequestParam(required = false) String keyword,
                                      @CookieValue(value = "username", defaultValue = "") String username,
                                      HttpSession session) {

        if (username.isEmpty() || !checkStudentRole(session, username)) {
            return "redirect:/login";
        }

        // Chuyển hướng về /student với registerCourseId để hiển thị modal
        String redirectUrl = "/student?page=" + page;
        if (keyword != null && !keyword.trim().isEmpty()) {
            redirectUrl += "&keyword=" + keyword;
        }
        redirectUrl += "&registerCourseId=" + id;
        return "redirect:" + redirectUrl;
    }

    // Xử lý đăng ký khóa học
    @PostMapping("/register")
    public String postRegister(@RequestParam("courseId") String courseId) {
        Student curUser = (Student) session.getAttribute("user");
        if (curUser == null) {
            return "redirect:/login";
        }

        Student student = studentService.findById(curUser.getId());
        if (student == null || !student.isStatus()) {
            return "redirect:/login";
        }

        Course course;
        try {
            course = courseService.getById(courseId);
        } catch (NumberFormatException e) {
            return "redirect:/student?error=invalid_course_id";
        }

        if (course == null || !course.getStatus()) {
            return "redirect:/student?error=invalid";
        }

        Enrollment existing = enrollmentService.existsByStudentAndCourse(student.getId(), courseId);
        if (existing != null) {
            if (existing.getStatus() == EnrollmentStatus.CONFIRM || existing.getStatus() == EnrollmentStatus.WAITING) {
                return "redirect:/student?error=already_registered";
            }
            existing.setStatus(EnrollmentStatus.WAITING);
            try {
                enrollmentService.updateEnrollment(existing);
            } catch (Exception e) {
                return "redirect:/student?error=update_failed";
            }
        } else {
            Enrollment enrollment = new Enrollment();
            enrollment.setCourse(course);
            enrollment.setStudent(student);
            try {
                enrollmentService.add(enrollment);
            } catch (Exception e) {
                return "redirect:/student?error=save_failed";
            }
        }

        return "redirect:/student?success";
    }

    // Hiển thị danh sách đăng ký khóa học
    @GetMapping("/enrollments")
    public String showEnrollments(@RequestParam(defaultValue = "1") int page,
                                  @RequestParam(required = false) String keyword,
                                  @RequestParam(defaultValue = "status") String sortField,
                                  @RequestParam(defaultValue = "true") boolean asc,
                                  @CookieValue(value = "username", defaultValue = "") String username,
                                  Model model,
                                  HttpSession session) {

        if (username.isEmpty() || !checkStudentRole(session, username)) {
            return "redirect:/login";
        }

        int size = 5;
        Student student = (Student) session.getAttribute("user");
        List<Enrollment> enrollments;
        int totalEnrollments;

        if (keyword != null && !keyword.trim().isEmpty()) {
            enrollments = enrollmentService.searchByCourseNameAndStudentId(keyword.trim(), student.getId(), page, size, sortField, asc);
            totalEnrollments = enrollmentService.countByCourseNameAndStudentId(keyword.trim(), student.getId());
        } else {
            enrollments = enrollmentService.findByStudentIdWithPaging(student.getId(), page, size, sortField, asc);
            totalEnrollments = enrollmentService.countByStudentId(student.getId());
        }

        int totalPages = (int) Math.ceil((double) totalEnrollments / size);

        model.addAttribute("enrollments", enrollments);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("keyword", keyword);
        model.addAttribute("sortField", sortField);
        model.addAttribute("asc", asc);

        return "student/enrollment";
    }

    // Hủy đăng ký khóa học
    @GetMapping("/cancel-enrollment")
    public String cancelEnrollment(@RequestParam("id") Integer enrollmentId,
                                   @RequestParam(defaultValue = "1") int page,
                                   @RequestParam(required = false) String keyword,
                                   @RequestParam(defaultValue = "status") String sortField,
                                   @RequestParam(defaultValue = "true") boolean asc,
                                   @CookieValue(value = "username", defaultValue = "") String username,
                                   HttpSession session) {

        if (username.isEmpty() || !checkStudentRole(session, username)) {
            return "redirect:/login";
        }

        Student student = (Student) session.getAttribute("user");
        Enrollment enrollment = enrollmentService.getEnrollmentByIdAndStudentId(enrollmentId, student.getId());

        if (enrollment == null || enrollment.getStatus() != EnrollmentStatus.WAITING) {
            return "redirect:/student/enrollments?page=" + page +
                    (keyword != null ? "&keyword=" + keyword : "") +
                    "&sortField=" + sortField + "&asc=" + asc + "&error=invalid";
        }

        enrollment.setStatus(EnrollmentStatus.CANCEL);
        try {
            enrollmentService.updateEnrollment(enrollment);
        } catch (Exception e) {
            return "redirect:/student/enrollments?page=" + page +
                    (keyword != null ? "&keyword=" + keyword : "") +
                    "&sortField=" + sortField + "&asc=" + asc + "&error=cancel_failed";
        }

        return "redirect:/student/enrollments?page=" + page +
                (keyword != null ? "&keyword=" + keyword : "") +
                "&sortField=" + sortField + "&asc=" + asc + "&success";
    }

    @GetMapping("/profile")
    public String profile(Model model, HttpSession session, @CookieValue(value = "username", defaultValue = "") String username) {
        if (username.isEmpty() || !checkStudentRole(session, username)) {
            return "redirect:/login";
        }

        Student student = (Student) session.getAttribute("user");
        if (student == null) {
            return "redirect:/login";
        }

        StudentDTO studentDto = new StudentDTO();
        studentDto.setUsername(student.getUsername());
        studentDto.setName(student.getName());
        studentDto.setDob(student.getDob());
        studentDto.setEmail(student.getEmail());
        studentDto.setPhone(student.getPhone());
        studentDto.setPassword(student.getPassword());
        studentDto.setSex(student.getSex());
        studentDto.setRole(student.getRole());
        studentDto.setCreateAt(student.getCreateAt());

        model.addAttribute("studentDto", studentDto);
        model.addAttribute("passwordDto", new PasswordDTO());
        model.addAttribute("isShowForm", false);

        return "student/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@Valid @ModelAttribute("studentDto") StudentDTO studentDto,
                                BindingResult result,
                                HttpSession session,
                                @CookieValue(value = "username", defaultValue = "") String username,
                                Model model) {
        if (username.isEmpty() || !checkStudentRole(session, username)) {
            return "redirect:/login";
        }

        Student student = (Student) session.getAttribute("user");
        if (student == null) {
            return "redirect:/login";
        }

        if (result.hasErrors()) {
            model.addAttribute("studentDto", studentDto);
            model.addAttribute("passwordDto", new PasswordDTO());
            model.addAttribute("isShowForm", false);
            return "student/profile";
        }

        // Update Student entity with DTO data
        student.setName(studentDto.getName());
        student.setEmail(studentDto.getEmail());
        student.setPhone(studentDto.getPhone());
        student.setSex(studentDto.getSex());
        student.setDob(studentDto.getDob());

        try {

            if (studentService.existsByEmail(studentDto.getEmail(), student.getId())) {
                model.addAttribute("error", "Email is already taken");
                model.addAttribute("studentDto", studentDto);
                model.addAttribute("passwordDto", new PasswordDTO());
                model.addAttribute("isShowForm", false);
                return "student/profile";
            }
            if (studentService.existsByPhone(studentDto.getPhone(), student.getId())) {
                model.addAttribute("error", "Phone number is already taken");
                model.addAttribute("studentDto", studentDto);
                model.addAttribute("passwordDto", new PasswordDTO());
                model.addAttribute("isShowForm", false);
            }


            studentService.updateStudent(student);
            // Update session with the new student data
            session.setAttribute("user", student);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("studentDto", studentDto);
            model.addAttribute("passwordDto", new PasswordDTO());
            model.addAttribute("isShowForm", false);
            return "student/profile";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to update profile");
            model.addAttribute("studentDto", studentDto);
            model.addAttribute("passwordDto", new PasswordDTO());
            model.addAttribute("isShowForm", false);
            return "student/profile";
        }

        return "redirect:/student/profile?success";
    }

    @GetMapping("/profile/change-password")
    public String showChangePasswordForm(Model model, HttpSession session, @CookieValue(value = "username", defaultValue = "") String username) {
        if (username.isEmpty() || !checkStudentRole(session, username)) {
            return "redirect:/login";
        }

        Student student = (Student) session.getAttribute("user");
        if (student == null) {
            return "redirect:/login";
        }

        StudentDTO studentDto = new StudentDTO();
        studentDto.setUsername(student.getUsername());
        studentDto.setName(student.getName());
        studentDto.setDob(student.getDob());
        studentDto.setEmail(student.getEmail());
        studentDto.setPhone(student.getPhone());
        studentDto.setPassword(student.getPassword());
        studentDto.setSex(student.getSex());
        studentDto.setRole(student.getRole());
        studentDto.setCreateAt(student.getCreateAt());

        model.addAttribute("studentDto", studentDto);
        model.addAttribute("passwordDto", new PasswordDTO());
        model.addAttribute("isShowForm", true);

        return "student/profile";
    }

    @PostMapping("/profile/change-password")
    public String changePassword(@Valid @ModelAttribute("passwordDto") PasswordDTO passwordDto,
                                 BindingResult result,
                                 HttpSession session,
                                 @CookieValue(value = "username", defaultValue = "") String username,
                                 Model model) {
        if (username.isEmpty() || !checkStudentRole(session, username)) {
            return "redirect:/login";
        }

        Student student = (Student) session.getAttribute("user");
        if (student == null) {
            return "redirect:/login";
        }

        StudentDTO studentDto = new StudentDTO();
        studentDto.setUsername(student.getUsername());
        studentDto.setName(student.getName());
        studentDto.setDob(student.getDob());
        studentDto.setEmail(student.getEmail());
        studentDto.setPhone(student.getPhone());
        studentDto.setPassword(student.getPassword());
        studentDto.setSex(student.getSex());
        studentDto.setRole(student.getRole());
        studentDto.setCreateAt(student.getCreateAt());

        if (result.hasErrors()) {
            model.addAttribute("studentDto", studentDto);
            model.addAttribute("passwordDto", passwordDto);
            model.addAttribute("isShowForm", true);
            return "student/profile";
        }

        // Validate old password
        if (!student.getPassword().equals(passwordDto.getOldPassword())) {
            model.addAttribute("errorForm", "Old password is incorrect");
            model.addAttribute("studentDto", studentDto);
            model.addAttribute("passwordDto", passwordDto);
            model.addAttribute("isShowForm", true);
            return "student/profile";
        }

        // Validate new password and confirm password match
        if (!passwordDto.getNewPassword().equals(passwordDto.getConfirmPassword())) {
            model.addAttribute("errorForm", "New password and confirm password do not match");
            model.addAttribute("studentDto", studentDto);
            model.addAttribute("passwordDto", passwordDto);
            model.addAttribute("isShowForm", true);
            return "student/profile";
        }

        try {
            studentService.updatePassword(student.getId(), passwordDto.getNewPassword());
            student.setPassword(passwordDto.getNewPassword());
            session.setAttribute("user", student);
        } catch (Exception e) {
            model.addAttribute("errorForm", "Failed to update password");
            model.addAttribute("studentDto", studentDto);
            model.addAttribute("passwordDto", passwordDto);
            model.addAttribute("isShowForm", true);
            return "student/profile";
        }

        return "redirect:/student/profile?success";
    }
}