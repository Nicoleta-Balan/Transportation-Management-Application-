package multitier.trans.repository;

import multitier.trans.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA Repository for the Reservation entity.
 * Provides CRUD methods (save, findById, etc.) automatically.
 */

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    /**
     * Custom query: Finds all reservations for a specific route.
     * Spring Data JPA will auto-generate the SQL:
     * "SELECT * FROM reservations WHERE route_id = ?"
     */

    List<Reservation> findByRouteId(Long routeId);

    /**
     * Custom query: Finds all reservations for a specific passenger.
     * Spring Data JPA will auto-generate the SQL:
     * "SELECT * FROM reservations WHERE passenger_name = ?"
     */
    List<Reservation> findByPassengerName(String passengerName);
}