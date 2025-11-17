package multitier.trans.repository;

import multitier.trans.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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

    /**
     * Calls the PostgreSQL function calculate_reservation_fare to get fare breakdown.
     * This allows the UI to display fare before saving the reservation.
     */
    @Query(value = "SELECT * FROM calculate_reservation_fare(:routeId, :passengerCategory, :seatCount, :reservationDate)", nativeQuery = true)
    Map<String, Object> calculateFare(
            @Param("routeId") Integer routeId,
            @Param("passengerCategory") String passengerCategory,
            @Param("seatCount") Integer seatCount,
            @Param("reservationDate") LocalDateTime reservationDate
    );
}