package multitier.trans.dto.auth;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RegisterRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "First name is required")
    @Size(min = 1, max = 100)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 1, max = 100)
    private String lastName;

    @Size(max = 20)
    private String phone;

    @Size(max = 500)
    private String address;

    private LocalDate dateOfBirth;
}
