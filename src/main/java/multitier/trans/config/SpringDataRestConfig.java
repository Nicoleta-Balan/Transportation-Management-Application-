package multitier.trans.config;

import multitier.trans.model.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

@Configuration
public class SpringDataRestConfig implements RepositoryRestConfigurer {

    @Override
    public void configureRepositoryRestConfiguration(
            RepositoryRestConfiguration config,
            CorsRegistry cors) {
        
        // Set base path for all REST endpoints
        config.setBasePath("/api");
        
        // Configure pagination defaults
        config.setDefaultPageSize(20);
        config.setMaxPageSize(100);
        
        // Expose entity IDs in responses (by default, IDs are hidden in HAL format)
        config.exposeIdsFor(
            Station.class,
            Route.class,
            Reservation.class,
            Timetable.class,
            RouteStop.class,
            TimetableStop.class,
            FarePolicy.class
        );
        
        config.setDefaultMediaType(org.springframework.http.MediaType.APPLICATION_JSON);
        
        // Enable all repository HTTP methods by default (GET, POST, PUT, DELETE) for repositories 
        // with @RepositoryRestResource(exported = true)
        // Custom @RepositoryRestController endpoints override Spring Data REST's automatic POST, PUT, DELETE endpoints.
        config.setExposeRepositoryMethodsByDefault(true);
        
        // Configure CORS
        cors.addMapping("/api/**")
            .allowedOrigins("*")
            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            .allowedHeaders("*");
    }
}