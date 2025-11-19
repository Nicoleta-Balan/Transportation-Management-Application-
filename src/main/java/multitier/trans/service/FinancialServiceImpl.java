package multitier.trans.service;

import multitier.trans.model.FarePolicy;
import multitier.trans.model.Reservation;
import multitier.trans.repository.FarePolicyRepository;
import multitier.trans.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Service Implementation ("The Brain") for financial logic.
 * Implements the workflow for SCRUM-34 (Computation Service).
 * Uses Spring Data JPA repositories for all data access operations.
 */
@Service
@Transactional(readOnly = true)
public class FinancialServiceImpl implements FinancialService {

    private final ReservationRepository reservationRepository;
    private final FarePolicyRepository farePolicyRepository;

    @Autowired
    public FinancialServiceImpl(ReservationRepository reservationRepository, FarePolicyRepository farePolicyRepository) {
        this.reservationRepository = reservationRepository;
        this.farePolicyRepository = farePolicyRepository;
    }

    /**
     * This is the "Computation Service" logic for SCRUM-34.
     * This version is fixed to use the correct getters from Reservation.
     */
    @Override
    public double calculateTotalRevenue() {
        List<Reservation> allReservations = reservationRepository.findAll();
        BigDecimal totalRevenue = BigDecimal.ZERO;

        for (Reservation res : allReservations) {

            // FIX: Call the correct method res.getStatus()
            if (multitier.trans.model.enums.ReservationStatus.CONFIRMED.equals(res.getStatus())) {

                // FIX: Call the correct getters from the 'res' object
                Optional<FarePolicy> policy = farePolicyRepository.findByRouteIdAndPassengerCategoryAndVehicleClass(
                        res.getRoute().getId(),
                        res.getPassengerCategory(), // <-- FIXED
                        res.getVehicleClass()     // <-- FIXED
                );

                if (policy.isPresent()) {
                    // FIX: Call the correct method res.getSeatCount()
                    BigDecimal seatCount = BigDecimal.valueOf(res.getSeatCount());
                    BigDecimal reservationRevenue = policy.get().getPrice().multiply(seatCount);
                    totalRevenue = totalRevenue.add(reservationRevenue);
                } else {
                    System.err.println("No fare policy found for reservation ID: " + res.getId());
                }
            }
        }
        return totalRevenue.doubleValue();
    }
}