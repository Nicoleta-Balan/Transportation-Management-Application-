package multitier.trans.repository;

import multitier.trans.model.ReservationStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationStatusHistoryRepository extends JpaRepository<ReservationStatusHistory, Long> {
    
    /**
     * Finds all status change history for a specific reservation.
     */
    List<ReservationStatusHistory> findByReservationIdOrderByChangedAtDesc(Long reservationId);
}

