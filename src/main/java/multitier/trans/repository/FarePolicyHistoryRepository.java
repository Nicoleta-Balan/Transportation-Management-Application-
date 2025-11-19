package multitier.trans.repository;

import multitier.trans.model.FarePolicyHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FarePolicyHistoryRepository extends JpaRepository<FarePolicyHistory, Long> {
    
    /**
     * Finds all history records for a specific fare policy.
     */
    List<FarePolicyHistory> findByFarePolicyIdOrderByChangedAtDesc(Long farePolicyId);
    
    /**
     * Finds all history records for a specific route.
     */
    List<FarePolicyHistory> findByRouteIdOrderByChangedAtDesc(Long routeId);
}

