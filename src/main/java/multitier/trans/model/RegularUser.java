package multitier.trans.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Regular User entity - extends User for standard users.
 * Stored in the same 'users' table with user_type = 'USER'
 * This is the default user type.
 */
@Entity
@DiscriminatorValue("USER")
public class RegularUser extends User {

    public RegularUser() {
        super();
    }

    public RegularUser(String username, String email, String passwordHash) {
        super(username, email, passwordHash);
        // Ensure role is set correctly for regular user
        setRole("USER");
    }

    /**
     * Override to return user role.
     */
    @Override
    public String getRole() {
        return "USER";
    }
}

