package multitier.trans.repository;

import multitier.trans.model.Reservation;
import multitier.trans.model.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Spring Data JPA Repository for the Reservation entity.
 * Provides CRUD methods (save, findById, etc.) automatically.
 */
@Repository
@RepositoryRestResource(
    path = "reservations",
    collectionResourceRel = "reservations",
    itemResourceRel = "reservation",
    exported = true  // Allow GET operations via Spring Data REST
)
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    /**
     * Disable POST/PUT operations - handled by ReservationRestController for DTOs and validation.
     * GET operations remain enabled via Spring Data REST.
     */
    @Override
    @RestResource(exported = false)
    <S extends Reservation> S save(S entity);

    @Override
    @RestResource(exported = false)
    <S extends Reservation> List<S> saveAll(Iterable<S> entities);

    /**
     * Disable DELETE operation - handled by ReservationRestController.
     */
    @Override
    @RestResource(exported = false)
    void deleteById(Long id);

    @Override
    @RestResource(exported = false)
    void delete(Reservation entity);

    @Override
    @RestResource(exported = false)
    void deleteAll(Iterable<? extends Reservation> entities);

    /**
     * Hide search methods - not needed as REST endpoints.
     */
    @RestResource(exported = false)
    List<Reservation> findByRouteId(Long routeId);

    @RestResource(exported = false)
    List<Reservation> findByPassengerName(String passengerName);

    @Query("SELECT DISTINCT r FROM Reservation r " +
           "JOIN r.route route " +
           "JOIN route.routeStops rs " +
           "WHERE rs.station.id = :stationId " +
           "AND r.status IN :statuses")
    @RestResource(exported = false)
    List<Reservation> findByStationIdAndStatusIn(
        @Param("stationId") Long stationId,
        @Param("statuses") List<ReservationStatus> statuses);

    /**
     * Find all reservations for a specific user
     */
    @RestResource(exported = false)
    List<Reservation> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Find active reservations for a user (PENDING or CONFIRMED with future departure)
     */
    @Query("SELECT r FROM Reservation r " +
           "WHERE r.user.id = :userId " +
           "AND r.status IN :statuses " +
           "AND r.tripDetails.departureTime >= :now " +
           "ORDER BY r.tripDetails.departureTime ASC")
    @RestResource(exported = false)
    List<Reservation> findActiveReservationsByUserId(
        @Param("userId") Long userId,
        @Param("statuses") List<ReservationStatus> statuses,
        @Param("now") LocalDateTime now);

    /**
     * Find past reservations for a user (completed trips)
     */
    @Query("SELECT r FROM Reservation r " +
           "WHERE r.user.id = :userId " +
           "AND r.tripDetails.departureTime < :now " +
           "ORDER BY r.tripDetails.departureTime DESC")
    @RestResource(exported = false)
    List<Reservation> findPastReservationsByUserId(
        @Param("userId") Long userId,
        @Param("now") LocalDateTime now);

    /**
     * Find confirmed reservations for a specific route and departure time to get occupied seats
     */
    @Query("SELECT r FROM Reservation r " +
           "WHERE r.route.id = :routeId " +
           "AND r.tripDetails.departureTime = :departureTime " +
           "AND r.status IN :statuses")
    @RestResource(exported = false)
    List<Reservation> findByRouteIdAndDepartureTimeAndStatusIn(
        @Param("routeId") Long routeId,
        @Param("departureTime") LocalDateTime departureTime,
        @Param("statuses") List<ReservationStatus> statuses);
}