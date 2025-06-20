package java_web.controller;

import java_web.config.CloudinaryService;
import java_web.dto.CourseDTO;
import java_web.dto.EnrollmentDTO;
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
import java.io.IOException;
import java.util.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final int PAGE_SIZE = 5;
    private static boolean isEdit = false;

    @Autowired
    private CourseService courseService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private EnrollmentService enrollmentService;

    @ModelAttribute("queryParams")
    public String buildQueryParams(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "id") String sortField,
            @RequestParam(defaultValue = "true") boolean asc,
            @RequestParam(required = false) String status) { // Thêm status vào queryParams
        StringBuilder params = new StringBuilder();
        params.append("page=").append(page);

        if (keyword != null && !keyword.isEmpty()) {
            params.append("&keyword=").append(keyword);
        }
        if (status != null && !status.isEmpty()) {
            params.append("&status=").append(status);
        }

        return params.append("&sortField=").append(sortField)
                .append("&asc=").append(asc)
                .toString();
    }

    @GetMapping("/courses")
    public String home(@RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "true") boolean status,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(defaultValue = "id") String sortField,
                       @RequestParam(defaultValue = "true") boolean asc,
                       Model model, HttpSession session,
                       @CookieValue(value = "username", defaultValue = "") String username) {

        if (!isAdmin(session, username)) {
            return "redirect:/login";
        }

        List<Course> courses;
        int totalCourses;

        if (keyword != null && !keyword.trim().isEmpty()) {
            courses = courseService.searchByName(keyword, status, page, PAGE_SIZE, sortField, asc);
            totalCourses = courseService.countByName(keyword, status);
        } else {
            courses = courseService.sortCourses(sortField, asc, status, page, PAGE_SIZE);
            totalCourses = courseService.countByStatus(status);
        }

        setupModelAttributes(model, page, status, keyword, sortField, asc, courses, totalCourses);
        model.addAttribute("isShowForm", false);
        return "admin/course";
    }

    @GetMapping("/courses/form")
    public String showForm(@RequestParam(defaultValue = "1") int page,
                           @RequestParam(defaultValue = "true") boolean status,
                           @RequestParam(required = false) String keyword,
                           @RequestParam(defaultValue = "id") String sortField,
                           @RequestParam(defaultValue = "true") boolean asc,
                           Model model, HttpSession session,
                           @CookieValue(value = "username", defaultValue = "") String username) {
        if (!isAdmin(session, username)) {
            return "redirect:/login";
        }
        model.addAttribute("courseDTO", new CourseDTO());
        isEdit = false;

        List<Course> courses;
        int totalCourses;

        if (keyword != null && !keyword.trim().isEmpty()) {
            courses = courseService.searchByName(keyword, status, page, PAGE_SIZE, sortField, asc);
            totalCourses = courseService.countByName(keyword, status);
        } else {
            courses = courseService.sortCourses(sortField, asc, status, page, PAGE_SIZE);
            totalCourses = courseService.countByStatus(status);
        }
        setupModelAttributes(model, page, status, keyword, sortField, asc, courses, totalCourses);

        model.addAttribute("isEdit", isEdit);
        model.addAttribute("isShowForm", true);
        return "admin/course";
    }

    @PostMapping("/courses/save")
    public String saveCourse(@RequestParam(defaultValue = "1") int page,
                             @RequestParam(defaultValue = "true") boolean status,
                             @RequestParam(required = false) String keyword,
                             @RequestParam(defaultValue = "id") String sortField,
                             @RequestParam(defaultValue = "true") boolean asc,
                             @Valid @ModelAttribute("courseDTO") CourseDTO courseDTO,
                             BindingResult result, Model model, HttpSession session,
                             @CookieValue(value = "username", defaultValue = "") String username)
            throws IOException {

        if (!isAdmin(session, username)) {
            return "redirect:/login";
        }

        List<Course> courses;
        int totalCourses;

        if (keyword != null && !keyword.trim().isEmpty()) {
            courses = courseService.searchByName(keyword, status, page, PAGE_SIZE, sortField, asc);
            totalCourses = courseService.countByName(keyword, status);
        } else {
            courses = courseService.sortCourses(sortField, asc, status, page, PAGE_SIZE);
            totalCourses = courseService.countByStatus(status);
        }
        setupModelAttributes(model, page, status, keyword, sortField, asc, courses, totalCourses);

        if (!isEdit) {
            if (courseService.existsByCourseId(courseDTO.getId())) {
                result.rejectValue("id", "duplicate.course.id",
                        "There is already a course with the id \"" + courseDTO.getId() + "\"");
            }

            if (courseService.existsByCourseName(courseDTO.getName())) {
                result.rejectValue("name", "duplicate.course.name",
                        "There is already a course with the name \"" + courseDTO.getName() + "\"");
            }
        }

        if (result.hasErrors()) {
            model.addAttribute("courseDTO", courseDTO);
            isEdit = false;
            model.addAttribute("isEdit", isEdit);
            model.addAttribute("isShowForm", true);
            return "admin/course";
        }

        Course course = mapDtoToEntity(courseDTO);

        if (courseDTO.getImage() != null && !courseDTO.getImage().isEmpty()) {
            course.setImage(cloudinaryService.uploadFile(courseDTO.getImage()));
        }
        courseService.save(course);
        model.addAttribute("isShowForm", false);
        return "redirect:/admin/courses";
    }

    @GetMapping("/courses/edit/{id}")
    public String editCourse(@RequestParam(defaultValue = "1") int page,
                             @RequestParam(defaultValue = "true") boolean status,
                             @RequestParam(required = false) String keyword,
                             @RequestParam(defaultValue = "id") String sortField,
                             @RequestParam(defaultValue = "true") boolean asc,
                             @PathVariable String id, Model model, HttpSession session,
                             @CookieValue(value = "username", defaultValue = "") String username) {

        if (!isAdmin(session, username)) {
            return "redirect:/login";
        }

        List<Course> courses;
        int totalCourses;

        if (keyword != null && !keyword.trim().isEmpty()) {
            courses = courseService.searchByName(keyword, status, page, PAGE_SIZE, sortField, asc);
            totalCourses = courseService.countByName(keyword, status);
        } else {
            courses = courseService.sortCourses(sortField, asc, status, page, PAGE_SIZE);
            totalCourses = courseService.countByStatus(status);
        }

        setupModelAttributes(model, page, status, keyword, sortField, asc, courses, totalCourses);

        Course course = courseService.getById(id);
        if (course == null) {
            return "redirect:/admin/courses";
        }

        model.addAttribute("courseDTO", mapEntityToDto(course));
        isEdit = true;
        model.addAttribute("isEdit", isEdit);
        model.addAttribute("isShowForm", true);

        return "admin/course";
    }

    @PostMapping("/courses/delete/{id}")
    public String delete(@PathVariable String id,
                         @RequestParam(defaultValue = "1") int page,
                         @RequestParam(defaultValue = "true") boolean status,
                         @RequestParam(required = false) String keyword,
                         @RequestParam(defaultValue = "id") String sortField,
                         @RequestParam(defaultValue = "true") boolean asc,
                         HttpSession session,
                         @CookieValue(value = "username", defaultValue = "") String username) {

        if (!isAdmin(session, username)) {
            return "redirect:/login";
        }

        courseService.softDelete(id);
        return buildRedirectUrl(page, status, keyword, sortField, asc, "courses");
    }

    @PostMapping("/courses/restore/{id}")
    public String restore(@PathVariable String id,
                          @RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "false") boolean status,
                          @RequestParam(required = false) String keyword,
                          @RequestParam(defaultValue = "id") String sortField,
                          @RequestParam(defaultValue = "true") boolean asc,
                          HttpSession session,
                          @CookieValue(value = "username", defaultValue = "") String username) {

        if (!isAdmin(session, username)) {
            return "redirect:/login";
        }

        courseService.restore(id);
        return buildRedirectUrl(page, status, keyword, sortField, asc, "courses");
    }

    @GetMapping("/students")
    public String showStudents(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "id") String sortField,
            @RequestParam(defaultValue = "true") boolean asc,
            Model model, HttpSession session,
            @CookieValue(value = "username", defaultValue = "") String username) {

        if (!isAdmin(session, username)) {
            return "redirect:/login";
        }

        List<Student> students;
        int totalStudents;

        if (keyword != null && !keyword.trim().isEmpty()) {
            students = studentService.searchStudents(keyword, page, PAGE_SIZE, sortField, asc);
            totalStudents = studentService.countByKeyword(keyword);
        } else {
            students = studentService.sortStudents(sortField, asc, page, PAGE_SIZE);
            totalStudents = studentService.countAll();
        }

        setupStudentModelAttributes(model, page, keyword, sortField, asc, students, totalStudents);

        return "admin/student";
    }

    @PostMapping("/students/toggle")
    public String toggleStudentStatus(
            @RequestParam("id") Integer id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "id") String sortField,
            @RequestParam(defaultValue = "true") boolean asc,
            HttpSession session,
            @CookieValue(value = "username", defaultValue = "") String username) {

        if (!isAdmin(session, username)) {
            return "redirect:/login";
        }

        studentService.toggleStatus(id);
        return buildRedirectUrl(page, true, keyword, sortField, asc, "students");
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session,
                            @CookieValue(value = "username", defaultValue = "") String username) {
        if (!isAdmin(session, username)) {
            return "redirect:/login";
        }
        model.addAttribute("totalCourses", courseService.countByStatus(true));
        model.addAttribute("totalStudents", studentService.countAll());
        model.addAttribute("totalEnrollments", enrollmentService.countAll());

        Map<Course, Integer> courseCountMap = new HashMap<>();
        List<Course> courses = courseService.findAll();
        for (Course course : courses) {
            int count = courseService.countByCourse(course.getId());
            courseCountMap.put(course, count);
        }
        model.addAttribute("courseCountMap", courseCountMap);

        List<Map.Entry<Course, Integer>> sortedList = new ArrayList<>(courseCountMap.entrySet());
        sortedList.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

        Map<Course, Integer> sortedCourseMap = new LinkedHashMap<>();
        for (Map.Entry<Course, Integer> entry : sortedList) {
            sortedCourseMap.put(entry.getKey(), entry.getValue());
        }

        Map<Course, Integer> topCourses = new LinkedHashMap<>();
        int i = 0;
        for (Map.Entry<Course, Integer> entry : sortedCourseMap.entrySet()) {
            if (i++ >= 5) break;
            topCourses.put(entry.getKey(), entry.getValue());
        }

        model.addAttribute("topCourses", topCourses);

        return "admin/dashboard";
    }

    private boolean isAdmin(HttpSession session, String username) {
        Student student = (Student) session.getAttribute("user");
        if (student == null && !username.isEmpty()) {
            student = studentService.findByUsername(username);
            if (student != null) {
                session.setAttribute("user", student);
            }
        }
        return student != null && student.getUsername().equals(username) && student.getRole();
    }

    private String buildRedirectUrl(int page, boolean status, String keyword, String sortField, boolean asc, String endpoint) {
        StringBuilder url = new StringBuilder("redirect:/admin/").append(endpoint).append("?page=").append(page);

        if (keyword != null && !keyword.isEmpty()) {
            url.append("&keyword=").append(keyword);
        }
        // Không thêm status vào URL vì đây là tham số của courses, không phải enrollments
        return url.append("&sortField=").append(sortField)
                .append("&asc=").append(asc)
                .toString();
    }

    private String buildEnrollmentRedirectUrl(int page, String keyword, String status, String sortField, boolean asc) {
        StringBuilder url = new StringBuilder("redirect:/admin/enrollments?page=").append(page);

        if (keyword != null && !keyword.isEmpty()) {
            url.append("&keyword=").append(keyword);
        }
        if (status != null && !status.isEmpty()) {
            url.append("&status=").append(status);
        }
        return url.append("&sortField=").append(sortField)
                .append("&asc=").append(asc)
                .toString();
    }

    private void setupModelAttributes(Model model, int page, boolean status,
                                      String keyword, String sortField, boolean asc,
                                      List<Course> courses, int totalCourses) {

        model.addAttribute("courses", courses);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", (int) Math.ceil((double) totalCourses / PAGE_SIZE));
        model.addAttribute("status", status);
        model.addAttribute("keyword", keyword);
        model.addAttribute("sortField", sortField);
        model.addAttribute("asc", asc);
    }

    private void setupStudentModelAttributes(Model model, int page,
                                             String keyword, String sortField, boolean asc,
                                             List<Student> students, int totalStudents) {

        model.addAttribute("students", students);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", (int) Math.ceil((double) totalStudents / PAGE_SIZE));
        model.addAttribute("keyword", keyword);
        model.addAttribute("sortField", sortField);
        model.addAttribute("asc", asc);
    }

    private Course mapDtoToEntity(CourseDTO dto) throws IOException {
        Course course = new Course();
        course.setId(dto.getId());
        course.setName(dto.getName());
        course.setDuration(dto.getDuration());
        course.setInstructor(dto.getInstructor());
        course.setCreateAt(dto.getCreateAt());

        if (dto.getImage() != null && !dto.getImage().isEmpty()) {
            course.setImage(cloudinaryService.uploadFile(dto.getImage()));
        }

        return course;
    }

    private CourseDTO mapEntityToDto(Course course) {
        CourseDTO dto = new CourseDTO();
        dto.setId(course.getId());
        dto.setName(course.getName());
        dto.setDuration(course.getDuration());
        dto.setInstructor(course.getInstructor());
        dto.setCreateAt(course.getCreateAt());
        return dto;
    }

    @GetMapping("/enrollments")
    public String enrollments(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "id") String sortField,
            @RequestParam(defaultValue = "true") boolean asc,
            @RequestParam(required = false) String status,
            Model model, HttpSession session,
            @CookieValue(value = "username", defaultValue = "") String username) {

        if (!isAdmin(session, username)) {
            return "redirect:/login";
        }

        List<EnrollmentDTO> enrollments = enrollmentService.findEnrollmentDTOsWithDetails(null, keyword, status, page, PAGE_SIZE, sortField, asc);
        int totalEnrollments = enrollmentService.countEnrollmentDTOsWithDetails(null, keyword, status);

        setupEnrollmentModelAttributes(model, page, keyword, sortField, asc, enrollments, totalEnrollments);
        model.addAttribute("status", status);

        return "admin/enrollment";
    }

    @PostMapping("/enrollments/confirm/{id}")
    public String confirmEnrollment(
            @PathVariable Integer id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "id") String sortField,
            @RequestParam(defaultValue = "true") boolean asc,
            @RequestParam(required = false) String status,
            HttpSession session,
            @CookieValue(value = "username", defaultValue = "") String username) {

        if (!isAdmin(session, username)) {
            return "redirect:/login";
        }

        Enrollment enrollment = enrollmentService.getEnrollmentById(id);
        if (enrollment != null && enrollment.getStatus() == EnrollmentStatus.WAITING) {
            enrollment.setStatus(EnrollmentStatus.CONFIRM);
            enrollmentService.updateEnrollment(enrollment);
        }

        return buildEnrollmentRedirectUrl(page, keyword, status, sortField, asc);
    }

    @PostMapping("/enrollments/deny/{id}")
    public String denyEnrollment(
            @PathVariable Integer id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "id") String sortField,
            @RequestParam(defaultValue = "true") boolean asc,
            @RequestParam(required = false) String status,
            HttpSession session,
            @CookieValue(value = "username", defaultValue = "") String username) {

        if (!isAdmin(session, username)) {
            return "redirect:/login";
        }

        Enrollment enrollment = enrollmentService.getEnrollmentById(id);
        if (enrollment != null && enrollment.getStatus() == EnrollmentStatus.WAITING) {
            enrollment.setStatus(EnrollmentStatus.DENIED);
            enrollmentService.updateEnrollment(enrollment);
        }

        return buildEnrollmentRedirectUrl(page, keyword, status, sortField, asc);
    }

    private void setupEnrollmentModelAttributes(Model model, int page,
                                                String keyword, String sortField, boolean asc,
                                                List<?> enrollments, int totalEnrollments) {
        model.addAttribute("enrollments", enrollments);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", (int) Math.ceil((double) totalEnrollments / PAGE_SIZE));
        model.addAttribute("keyword", keyword);
        model.addAttribute("sortField", sortField);
        model.addAttribute("asc", asc);
    }
}
