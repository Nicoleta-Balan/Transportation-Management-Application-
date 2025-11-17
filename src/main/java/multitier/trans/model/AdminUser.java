package multitier.trans.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Admin User entity - extends User with admin-specific capabilities.
 * Stored in the same 'users' table with user_type = 'ADMIN'
 */
@Entity
@DiscriminatorValue("ADMIN")
public class AdminUser extends User {

    public AdminUser() {
        super();
    }

    public AdminUser(String username, String email, String passwordHash) {
        super(username, email, passwordHash);
        // Ensure role is set correctly for admin
        setRole("ADMIN");
    }

    /**
     * Override to return admin role.
     */
    @Override
    public String getRole() {
        return "ADMIN";
    }
}

