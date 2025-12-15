package multitier.trans.repository;

import multitier.trans.model.Reservation;
import multitier.trans.model.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.stereotype.Repository;

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
}