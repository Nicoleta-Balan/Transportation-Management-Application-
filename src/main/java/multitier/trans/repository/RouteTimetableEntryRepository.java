package multitier.trans.repository;

import multitier.trans.model.RouteTimetableEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RouteTimetableEntryRepository extends JpaRepository<RouteTimetableEntry, Long> {
}

