package java_web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PasswordDTO{
    @NotBlank(message = "Old password can't be empty")
    private String oldPassword;

    @Pattern(
            regexp = "^$|^(?=.*[a-zA-Z])(?=.*[\\d_@#-]).{8,}$",
            message = "New password must be at least 8 characters and include at least one letter and one number or special character (_, -, @, #)"
    )
    @NotBlank(message = "New password can't be empty")
    private String newPassword;

    @NotBlank(message = "Confirm password can't be empty")
    private String confirmPassword;

}