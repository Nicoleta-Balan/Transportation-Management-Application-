package multitier.trans.repository;

import multitier.trans.model.Timetable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RepositoryRestResource(
    path = "timetables",
    collectionResourceRel = "timetables",
    itemResourceRel = "timetable",
    exported = true  // Allow GET operations via Spring Data REST
)
public interface TimetableRepository extends JpaRepository<Timetable, Long> {
    
    // Disable POST/PUT operations - handled by TimetableRestController for DTOs and validation.
    // GET operations remain enabled via Spring Data REST.
    @Override
    @RestResource(exported = false)
    <S extends Timetable> S save(S entity);
    
    @Override
    @RestResource(exported = false)
    <S extends Timetable> List<S> saveAll(Iterable<S> entities);
    
    // Disable DELETE operation - handled by TimetableRestController.
    @Override
    @RestResource(exported = false)
    void deleteById(Long id);
    
    @Override
    @RestResource(exported = false)
    void delete(Timetable entity);
    
    @Override
    @RestResource(exported = false)
    void deleteAll(Iterable<? extends Timetable> entities);

    // Custom query to find timetables by route with stops eagerly loaded.
    // Exposed as a search endpoint: GET /api/timetables/search/findByRouteIdWithStops?routeId={routeId}
    @Query("SELECT DISTINCT t FROM Timetable t " +
           "LEFT JOIN FETCH t.timetableStops ts " +
           "LEFT JOIN FETCH ts.station " +
           "LEFT JOIN FETCH t.route " +
           "WHERE t.route.id = :routeId ORDER BY t.id")
    @RestResource(exported = true, path = "findByRouteIdWithStops", rel = "findByRouteIdWithStops")
    List<Timetable> findByRouteIdWithStops(@Param("routeId") Long routeId);

    // Custom query to find a timetable by ID with stops eagerly loaded.
    // Exposed as a search endpoint: GET /api/timetables/search/findByIdWithStops?id={id}
    @Query("SELECT DISTINCT t FROM Timetable t " +
           "LEFT JOIN FETCH t.timetableStops ts " +
           "LEFT JOIN FETCH ts.station " +
           "LEFT JOIN FETCH t.route " +
           "WHERE t.id = :id")
    @RestResource(exported = true, path = "findByIdWithStops", rel = "findByIdWithStops")
    Optional<Timetable> findByIdWithStops(@Param("id") Long id);

    // Native query to search timetables by stations and date
    // Using native query to bypass potential JPQL mapping issues and ensure direct SQL execution
    // against the known database schema.
    @Query(value = "SELECT t.* FROM timetables t " +
           "JOIN routes r ON t.route_id = r.id " +
           "WHERE r.origin_station_id = :fromStationId " +
           "AND r.destination_station_id = :toStationId " +
           "AND (t.start_date IS NULL OR t.start_date <= :date) " +
           "AND (t.end_date IS NULL OR t.end_date >= :date)", 
           nativeQuery = true)
    @RestResource(exported = false)
    List<Timetable> findBySearch(
        @Param("fromStationId") Long fromStationId,
        @Param("toStationId") Long toStationId,
        @Param("date") LocalDate date
    );
}
