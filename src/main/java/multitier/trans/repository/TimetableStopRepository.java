package multitier.trans.repository;

import multitier.trans.model.TimetableStop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RepositoryRestResource(exported = false)  // Disable automatic REST exposure - TimetableStop is part of Timetable aggregate
public interface TimetableStopRepository extends JpaRepository<TimetableStop, Long> {

    List<TimetableStop> findByTimetableIdOrderBySequenceOrderAsc(Long timetableId);
}

