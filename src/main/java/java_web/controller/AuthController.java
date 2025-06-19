package java_web.controller;

import java_web.dto.StudentDTO;
import java_web.entity.Student;
import java_web.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@Controller
public class AuthController {

    @Autowired
    private StudentService studentService;

    @GetMapping("/register")
    public String registerPage(Model model) {
        if (!model.containsAttribute("studentdto")) {
            model.addAttribute("studentdto", new StudentDTO());
        }
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("studentdto") StudentDTO dto,
                           BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("error", "Vui lòng sửa các lỗi trong biểu mẫu");
            return "auth/register";
        }

        Student student = new Student();
        student.setUsername(dto.getUsername());
        student.setName(dto.getName());
        student.setEmail(dto.getEmail());
        student.setPhone(dto.getPhone());
        student.setPassword(dto.getPassword());
        student.setDob(dto.getDob());
        student.setSex(dto.getSex());
        student.setRole(dto.getRole());
        student.setCreateAt(dto.getCreateAt());

        try {
            boolean success = studentService.register(student);
            redirectAttributes.addFlashAttribute("success", "Đăng ký thành công. Vui lòng đăng nhập.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "auth/register";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra trong quá trình đăng ký: " + e.getMessage());
            return "auth/register";
        }
    }

    @GetMapping("/login")
    public String loginPage(Model model, @CookieValue(value = "username", defaultValue = "") String username) {
        if (!username.isEmpty()) {
            Student student = studentService.findByUsername(username);
            if (student != null) {
                return student.getRole() ? "redirect:/admin/courses" : "redirect:/student";
            }
        }
        if (!model.containsAttribute("studentdto")) {
            model.addAttribute("studentdto", new StudentDTO());
        }
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute("studentdto") StudentDTO dto,
                        BindingResult result, Model model, HttpSession session,
                        HttpServletResponse response, RedirectAttributes redirectAttributes) {
        if (result.hasFieldErrors("username") || result.hasFieldErrors("password")) {
            model.addAttribute("error", "Tên đăng nhập và mật khẩu là bắt buộc");
            return "auth/login";
        }

        try {
            Student student = studentService.login(dto.getUsername(), dto.getPassword());
            if (student == null) {
                model.addAttribute("error", "Tên đăng nhập hoặc mật khẩu không đúng");
                return "auth/login";
            }

            // Lưu username vào cookie
            Cookie loginCookie = new Cookie("username", student.getUsername());
            loginCookie.setMaxAge(3 * 24 * 60 * 60); // 3 ngày
            loginCookie.setPath("/");
            loginCookie.setHttpOnly(true);
            loginCookie.setSecure(true);
            response.addCookie(loginCookie);



            // Lưu Student vào session
            session.setAttribute("user", student);

            redirectAttributes.addFlashAttribute("success", "Đăng nhập thành công");
            return student.getRole() ? "redirect:admin/courses" : "redirect:/student";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra trong quá trình đăng nhập: " + e.getMessage());
            return "auth/login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, HttpServletResponse response, RedirectAttributes redirectAttributes) {
        // Xóa cookie
        Cookie cookie = new Cookie("username", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        response.addCookie(cookie);

        // Hủy session
        session.invalidate();

        redirectAttributes.addFlashAttribute("success", "Đăng xuất thành công");
        return "redirect:/login";
    }
}