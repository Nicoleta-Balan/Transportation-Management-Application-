package multitier.trans.dto.auth;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateProfileRequest {

    @Size(min = 1, max = 100, message = "First name must be between 1 and 100 characters")
    private String firstName;

    @Size(min = 1, max = 100, message = "Last name must be between 1 and 100 characters")
    private String lastName;

    @Size(max = 20, message = "Phone must be less than 20 characters")
    private String phone;

    @Size(max = 500, message = "Address must be less than 500 characters")
    private String address;

    private LocalDate dateOfBirth;
}
