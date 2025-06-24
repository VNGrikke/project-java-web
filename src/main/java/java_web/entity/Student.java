package java_web.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "student")
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "dob", nullable = false)
    private LocalDate dob;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "sex", nullable = false)
    private Boolean sex;

    @Column(name = "phone", unique = true, length = 20, nullable = false)
    private String phone;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "create_at", nullable = false)
    private LocalDate createAt = LocalDate.now();

    @Column(name = "role", nullable = false)
    private Boolean role;

    @Column(name = "status")
    private boolean status = true;

    public String getFormatdob(){
        return dob.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
    }
}