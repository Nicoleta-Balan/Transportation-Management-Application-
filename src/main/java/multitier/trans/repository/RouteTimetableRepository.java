package multitier.trans.repository;

import multitier.trans.model.RouteTimetable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RouteTimetableRepository extends JpaRepository<RouteTimetable, Long> {

    @EntityGraph(attributePaths = "entries")
    List<RouteTimetable> findByRouteId(Long routeId);

    @EntityGraph(attributePaths = "entries")
    Optional<RouteTimetable> findByIdAndRouteId(Long id, Long routeId);
}

