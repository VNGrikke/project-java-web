package java_web.entity;

import java_web.service.CourseService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "course")
public class Course {
    @Id
    private String id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "duration", nullable = false)
    private Integer duration;

    @Column(name = "instructor", nullable = false, length = 100)
    private String instructor;

    @Column(name = "create_at", nullable = false)
    private LocalDate createAt = LocalDate.now();

    @Column(name = "image", length = 500)
    private String image = null;

    @Column(name = "status")
    private boolean status = true;

    public String getFormat(){
        return createAt.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
    }

    @Transient
    @ToString.Exclude
    private boolean registered;

    public boolean getStatus() {
        return status;
    }


}