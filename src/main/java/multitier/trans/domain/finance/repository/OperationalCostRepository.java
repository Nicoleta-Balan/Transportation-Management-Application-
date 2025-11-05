package multitier.trans.domain.finance.repository;

import multitier.trans.domain.finance.model.OperationalCost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OperationalCostRepository extends JpaRepository<OperationalCost, Long> {
}

