package ro.ase.acs.mind_path.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StudentCreationDto {
    @Email(message = "Email must contain a valid email address")
    private String email;
    @Size(min = 4, max = 20, message = "Password length should be between 4 and 20 characters")
    private String password;
    @Pattern(regexp = "STUDENT", message = "The user type of a student must be set to STUDENT")
    private String userType;
    @NotBlank(message = "First name cannot be blank")
    private String firstName;
    @NotBlank(message = "Last name cannot be blank")
    private String lastName;
}
