package multitier.trans.repository; // Your package name (with capital R)

// Import the Model
import multitier.trans.model.Station;

// Import Spring Data JPA
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

//CRUD (Create, Read, Update, Delete) methods automatically by extending JpaRepository
@Repository
public interface StationRepository extends JpaRepository<Station, Long> {

    //"SELECT * FROM stations WHERE name = @param"
    Station findByName(String name);
}