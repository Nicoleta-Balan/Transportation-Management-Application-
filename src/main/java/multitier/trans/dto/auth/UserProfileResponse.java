package multitier.trans.dto.auth;

import lombok.Builder;
import lombok.Data;
import multitier.trans.model.enums.UserType;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class UserProfileResponse {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String address;
    private LocalDate dateOfBirth;
    private UserType userType;
    private LocalDateTime createdAt;
}
