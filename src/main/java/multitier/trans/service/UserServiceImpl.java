package multitier.trans.service;

import multitier.trans.dto.RegisterRequest;
import multitier.trans.model.User;
import multitier.trans.model.RegularUser;
import multitier.trans.model.AdminUser;
import multitier.trans.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public User register(RegisterRequest request) {
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists: " + request.getUsername());
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists: " + request.getEmail());
        }

        // Create new RegularUser (default user type)
        // Using inheritance - RegularUser extends User
        RegularUser user = new RegularUser(
                request.getUsername(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword())
        );
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setEnabled(true);

        return userRepository.save(user);
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    @Transactional
    public void updateLastLogin(String username) {
        User user = findByUsername(username);
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    public User createAdminUser(String username, String email, String password, String firstName, String lastName) {
        // Check if username already exists
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists: " + username);
        }

        // Check if email already exists
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists: " + email);
        }

        // Create AdminUser using inheritance
        AdminUser adminUser = new AdminUser(
                username,
                email,
                passwordEncoder.encode(password)
        );
        adminUser.setFirstName(firstName);
        adminUser.setLastName(lastName);
        adminUser.setEnabled(true);

        return userRepository.save(adminUser);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = findByUsername(username);

        if (!user.getEnabled()) {
            throw new UsernameNotFoundException("User is disabled: " + username);
        }

        // getRole() now returns role based on entity type (AdminUser vs RegularUser)
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPasswordHash())
                .roles(user.getRole())
                .disabled(!user.getEnabled())
                .build();
    }
}

