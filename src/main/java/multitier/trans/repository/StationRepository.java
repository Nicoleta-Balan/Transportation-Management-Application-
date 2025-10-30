package multitier.trans.repository; // Your package name (with capital R)

// Import the Model
import multitier.trans.model.Station;

// Import Spring Data JPA
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * This is the Repository interface for the Station entity.
 * It provides all standard CRUD (Create, Read, Update, Delete) methods
 * automatically by extending JpaRepository.
 * This file is corrected to have only one interface definition.
 */
@Repository
public interface StationRepository extends JpaRepository<Station, Long> {

    /**
     * By naming it 'findByName', Spring Data JPA will automatically
     * generate the SQL query: "SELECT * FROM stations WHERE name = ?"
     *
     * @param name The name of the station to search for.
     * @return A Station object if found, or null.
     */
    Station findByName(String name);
}