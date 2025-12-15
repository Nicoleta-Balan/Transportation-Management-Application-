package multitier.trans.repository;

import multitier.trans.model.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RepositoryRestResource(
    path = "stations",
    collectionResourceRel = "stations",
    itemResourceRel = "station",
    exported = true  // Allow GET operations via Spring Data REST
)
public interface StationRepository extends JpaRepository<Station, Long> {
    
    //Disable POST/PUT operations - handled by StationRestController for DTOs and validation.
    @Override
    @RestResource(exported = false)
    <S extends Station> S save(S entity);
    
    @Override
    @RestResource(exported = false)
    <S extends Station> List<S> saveAll(Iterable<S> entities);
    
    // Disable DELETE operation - handled by StationRestController.
    @Override
    @RestResource(exported = false)
    void deleteById(Long id);
    
    @Override
    @RestResource(exported = false)
    void delete(Station entity);
    
    @Override
    @RestResource(exported = false)
    void deleteAll(Iterable<? extends Station> entities);

    //Hide search methods - not needed as REST endpoints.
    @RestResource(exported = false)
    Station findByName(String name);

    @RestResource(exported = false)
    Station findByAddress(String address);

    @RestResource(exported = false)
    Station findByAddressAndIdNot(String address, Long id);
}