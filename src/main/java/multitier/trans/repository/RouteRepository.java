package multitier.trans.repository;

import multitier.trans.model.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Data Access Layer (Repository) for the Route entity.
 * JpaRepository provides automatic CRUD (Create, Read, Update, Delete) methods
 * for the Route table without writing any SQL or implementation code.
 * It works with the Route entity and its Long primary key.
 */
@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {
    // This interface is empty, but it now has methods like save(), findAll(), and findById().

    boolean existsByOriginStationId(Long stationId);

    boolean existsByDestinationStationId(Long stationId);
}