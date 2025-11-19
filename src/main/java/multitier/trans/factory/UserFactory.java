package multitier.trans.factory;

import multitier.trans.model.User;
import multitier.trans.model.RegularUser;
import multitier.trans.model.AdminUser;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Factory for creating User entities.
 * 
 * Factory Pattern Benefits:
 * 1. Encapsulates object creation logic
 * 2. Centralizes user creation rules
 * 3. Makes it easy to add new user types in the future
 * 4. Separates creation logic from business logic
 * 5. Makes testing easier (can mock the factory)
 * 
 * This factory handles:
 * - Creating RegularUser instances
 * - Creating AdminUser instances
 * - Password encoding
 * - Setting default values
 */
@Component
public class UserFactory {

    private final PasswordEncoder passwordEncoder;

    public UserFactory(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Creates a RegularUser with the provided information.
     * 
     * @param username Username (must be unique)
     * @param email Email address (must be unique)
     * @param password Plain text password (will be encoded)
     * @param firstName First name (optional)
     * @param lastName Last name (optional)
     * @param phone Phone number (optional)
     * @return A new RegularUser instance (not yet persisted)
     */
    public RegularUser createRegularUser(
            String username,
            String email,
            String password,
            String firstName,
            String lastName,
            String phone) {
        
        RegularUser user = new RegularUser(
                username,
                email,
                passwordEncoder.encode(password)
        );
        
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPhone(phone);
        user.setEnabled(true);
        
        return user;
    }

    /**
     * Creates an AdminUser with the provided information.
     * 
     * @param username Username (must be unique)
     * @param email Email address (must be unique)
     * @param password Plain text password (will be encoded)
     * @param firstName First name (optional)
     * @param lastName Last name (optional)
     * @return A new AdminUser instance (not yet persisted)
     */
    public AdminUser createAdminUser(
            String username,
            String email,
            String password,
            String firstName,
            String lastName) {
        
        AdminUser adminUser = new AdminUser(
                username,
                email,
                passwordEncoder.encode(password)
        );
        
        adminUser.setFirstName(firstName);
        adminUser.setLastName(lastName);
        adminUser.setEnabled(true);
        
        return adminUser;
    }

    /**
     * Creates a User based on the user type string.
     * This is useful when the user type is determined dynamically.
     * 
     * @param userType "USER" for RegularUser, "ADMIN" for AdminUser
     * @param username Username
     * @param email Email
     * @param password Plain text password
     * @param firstName First name
     * @param lastName Last name
     * @param phone Phone (optional, only for regular users)
     * @return A new User instance (not yet persisted)
     * @throws IllegalArgumentException if userType is invalid
     */
    public User createUser(
            String userType,
            String username,
            String email,
            String password,
            String firstName,
            String lastName,
            String phone) {
        
        if ("ADMIN".equalsIgnoreCase(userType)) {
            return createAdminUser(username, email, password, firstName, lastName);
        } else if ("USER".equalsIgnoreCase(userType)) {
            return createRegularUser(username, email, password, firstName, lastName, phone);
        } else {
            throw new IllegalArgumentException("Invalid user type: " + userType + ". Must be 'USER' or 'ADMIN'");
        }
    }
}

