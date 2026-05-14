package multitier.trans.config;

import lombok.RequiredArgsConstructor;
import multitier.trans.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    @Value("${cors.allowed-origins:http://localhost:5173,http://localhost:3000}")
    private String allowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                // Allow CORS preflight requests without authentication
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // Public endpoints - Authentication
                .requestMatchers("/api/auth/**").permitAll()

                // Public endpoints - API Documentation
                .requestMatchers("/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                // Public endpoints - Read-only access (users can search without login)
                .requestMatchers(HttpMethod.GET, "/api/stations/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/routes/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/timetables/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/search-api/**").permitAll()
                .requestMatchers("/api/exchange-rates/**").permitAll()
                .requestMatchers("/api/payments/**").permitAll()

                // Seat management - allow public access for booking flow
                .requestMatchers("/api/seats/**").permitAll()

                // Admin-only endpoints - Station management
                .requestMatchers(HttpMethod.POST, "/api/stations/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/stations/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/stations/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/stations/**").hasRole("ADMIN")

                // Admin-only endpoints - Route management
                .requestMatchers(HttpMethod.POST, "/api/routes/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/routes/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/routes/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/routes/**").hasRole("ADMIN")

                // Admin-only endpoints - Timetable management
                .requestMatchers(HttpMethod.POST, "/api/timetables/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/timetables/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/timetables/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/timetables/**").hasRole("ADMIN")

                // User endpoints (require authentication)
                .requestMatchers("/api/user/**").authenticated()
                .requestMatchers("/api/reservations/**").authenticated()

                // All other endpoints require authentication
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allow ALL origins using pattern - works with dynamic IPs and no domain
        // The "*" pattern with setAllowedOriginPatterns works even with credentials
        configuration.setAllowedOriginPatterns(List.of("*"));

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
