package multitier.trans.repository;

import multitier.trans.model.SeatHold;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SeatHoldRepository extends JpaRepository<SeatHold, Long> {

    /**
     * Find all active (non-expired) seat holds for a specific route and departure time
     */
    @Query("SELECT sh FROM SeatHold sh " +
           "WHERE sh.route.id = :routeId " +
           "AND sh.departureTime = :departureTime " +
           "AND sh.expiresAt > :now")
    List<SeatHold> findActiveHoldsByRouteAndDeparture(
        @Param("routeId") Long routeId,
        @Param("departureTime") LocalDateTime departureTime,
        @Param("now") LocalDateTime now);

    /**
     * Find all active seat holds for a session
     */
    @Query("SELECT sh FROM SeatHold sh " +
           "WHERE sh.sessionId = :sessionId " +
           "AND sh.expiresAt > :now")
    List<SeatHold> findActiveHoldsBySession(
        @Param("sessionId") String sessionId,
        @Param("now") LocalDateTime now);

    /**
     * Check if a specific seat is already held for a route/departure
     */
    @Query("SELECT sh FROM SeatHold sh " +
           "WHERE sh.route.id = :routeId " +
           "AND sh.departureTime = :departureTime " +
           "AND sh.seatNumber = :seatNumber " +
           "AND sh.expiresAt > :now")
    Optional<SeatHold> findActiveSeatHold(
        @Param("routeId") Long routeId,
        @Param("departureTime") LocalDateTime departureTime,
        @Param("seatNumber") String seatNumber,
        @Param("now") LocalDateTime now);

    /**
     * Delete all expired seat holds (cleanup job)
     */
    @Modifying
    @Query("DELETE FROM SeatHold sh WHERE sh.expiresAt <= :now")
    int deleteExpiredHolds(@Param("now") LocalDateTime now);

    /**
     * Delete all seat holds for a session (when booking is completed or cancelled)
     */
    @Modifying
    @Query("DELETE FROM SeatHold sh WHERE sh.sessionId = :sessionId")
    int deleteBySessionId(@Param("sessionId") String sessionId);

    /**
     * Get all held seat numbers for a route/departure (just the seat numbers)
     */
    @Query("SELECT sh.seatNumber FROM SeatHold sh " +
           "WHERE sh.route.id = :routeId " +
           "AND sh.departureTime = :departureTime " +
           "AND sh.expiresAt > :now")
    List<String> findHeldSeatNumbers(
        @Param("routeId") Long routeId,
        @Param("departureTime") LocalDateTime departureTime,
        @Param("now") LocalDateTime now);

    /**
     * Get all held seat numbers for a route/departure using time range (handles precision differences)
     */
    @Query("SELECT sh.seatNumber FROM SeatHold sh " +
           "WHERE sh.route.id = :routeId " +
           "AND sh.departureTime >= :startTime " +
           "AND sh.departureTime <= :endTime " +
           "AND sh.expiresAt > :now")
    List<String> findHeldSeatNumbersByTimeRange(
        @Param("routeId") Long routeId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime,
        @Param("now") LocalDateTime now);
}
