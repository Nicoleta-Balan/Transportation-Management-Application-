package multitier.trans.repository;

import multitier.trans.model.RouteStop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RepositoryRestResource(exported = false)  // Disable automatic REST exposure - RouteStop is part of Route aggregate
public interface RouteStopRepository extends JpaRepository<RouteStop, Long> {

    List<RouteStop> findByRouteIdOrderBySequenceOrderAsc(Long routeId);

    @Modifying
    @Query("DELETE FROM RouteStop rs WHERE rs.route.id = :routeId")
    void deleteByRouteId(@Param("routeId") Long routeId);
}

