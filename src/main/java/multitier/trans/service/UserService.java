package multitier.trans.service;

import multitier.trans.dto.RegisterRequest;
import multitier.trans.model.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {
    User register(RegisterRequest request);
    User findByUsername(String username);
    void updateLastLogin(String username);
    User createAdminUser(String username, String email, String password, String firstName, String lastName);
}

