package multitier.trans.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import multitier.trans.model.enums.UserType;

@Data
@Builder
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String email;
    private String firstName;
    private String lastName;
    private UserType userType;
}
