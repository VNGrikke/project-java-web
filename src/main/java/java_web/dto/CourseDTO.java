package java_web.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@Data
public class CourseDTO {

    @NotBlank(message = "Id is required")
    @Size(max = 5, message = "Id must not exceed 5 characters")
    private String id;

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @Max(value = 1000, message = "Duration must not exceed 1000")
    private Integer duration;

    @NotBlank(message = "Instructor is required")
    @Size(max = 100, message = "Instructor must not exceed 100 characters")
    private String instructor;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate createAt = LocalDate.now() ;

    private MultipartFile image;
}
