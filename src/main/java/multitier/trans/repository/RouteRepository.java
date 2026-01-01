package multitier.trans.repository;

import multitier.trans.model.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RepositoryRestResource(
    path = "routes",
    collectionResourceRel = "routes",
    itemResourceRel = "route",
    exported = true  // Allow GET operations via Spring Data REST
)
public interface RouteRepository extends JpaRepository<Route, Long> {
    
    // Disable POST/PUT operations - handled by RouteRestController for DTOs and validation.
    // GET operations remain enabled via Spring Data REST.
    @Override
    @RestResource(exported = false)
    <S extends Route> S save(S entity);
    
    @Override
    @RestResource(exported = false)
    <S extends Route> List<S> saveAll(Iterable<S> entities);
    
    // Disable DELETE operation - handled by RouteRestController.
    @Override
    @RestResource(exported = false)
    void deleteById(Long id);
    
    @Override
    @RestResource(exported = false)
    void delete(Route entity);
    
    @Override
    @RestResource(exported = false)
    void deleteAll(Iterable<? extends Route> entities);
    
    // Custom query to find all routes with stops eagerly loaded.
    // Exposed as a search endpoint: GET /api/routes/search/allWithStops
    @Query("SELECT DISTINCT r FROM Route r LEFT JOIN FETCH r.routeStops rs LEFT JOIN FETCH rs.station ORDER BY r.id")
    @RestResource(exported = true, path = "allWithStops", rel = "allWithStops")
    List<Route> findAllWithStops();

    // Custom query to find a route by ID with stops eagerly loaded.
    // Exposed as a search endpoint: GET /api/routes/search/findByIdWithStops?id={id}
    @Query("SELECT DISTINCT r FROM Route r LEFT JOIN FETCH r.routeStops rs LEFT JOIN FETCH rs.station WHERE r.id = :id")
    @RestResource(exported = true, path = "findByIdWithStops", rel = "findByIdWithStops")
    Optional<Route> findByIdWithStops(@Param("id") Long id);

    // Find routes by origin and destination station IDs
    // Exposed as: GET /api/routes/search/byStations?originId={originId}&destinationId={destinationId}
    @RestResource(path = "byStations", rel = "byStations")
    List<Route> findByOriginStationIdAndDestinationStationId(
        @Param("originId") Long originId, 
        @Param("destinationId") Long destinationId
    );
}
