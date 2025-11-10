package multitier.trans.repository;

import multitier.trans.model.RouteAvailability;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RouteAvailabilityRepository extends JpaRepository<RouteAvailability, Long> {
}

