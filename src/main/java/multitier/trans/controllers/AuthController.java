package multitier.trans.controllers;

import jakarta.validation.Valid;
import multitier.trans.dto.AuthResponse;
import multitier.trans.dto.LoginRequest;
import multitier.trans.dto.RegisterRequest;
import multitier.trans.model.User;
import multitier.trans.service.UserService;
import multitier.trans.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public AuthController(UserService userService, JwtUtil jwtUtil, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = userService.register(request);
            String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
            
            AuthResponse response = new AuthResponse(
                token,
                user.getUsername(),
                user.getRole(),
                "User registered successfully"
            );
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            AuthResponse response = new AuthResponse();
            response.setMessage("Registration failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getUsername(),
                    request.getPassword()
                )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            User user = userService.findByUsername(request.getUsername());
            String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
            
            // Update last login
            userService.updateLastLogin(user.getUsername());
            
            AuthResponse response = new AuthResponse(
                token,
                user.getUsername(),
                user.getRole(),
                "Login successful"
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            AuthResponse response = new AuthResponse();
            response.setMessage("Login failed: Invalid username or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
}

