package multitier.trans.service;

import multitier.trans.model.FarePolicy;
import multitier.trans.model.Reservation;
import multitier.trans.model.enums.ReservationStatus;
import multitier.trans.repository.FarePolicyRepository;
import multitier.trans.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service Implementation ("The Brain") for financial logic.
 */

@Service
public class FinancialServiceImpl implements FinancialService {

    private final ReservationRepository reservationRepository;
    private final FarePolicyRepository farePolicyRepository;

    @Autowired
    public FinancialServiceImpl(ReservationRepository reservationRepository, FarePolicyRepository farePolicyRepository) {
        this.reservationRepository = reservationRepository;
        this.farePolicyRepository = farePolicyRepository;
    }

    /**
     * This is the "Computation Service" logic
     * This version is fixed to use the correct getters from Reservation.
     */

    @Override
    public double calculateTotalRevenue() {
        List<Reservation> allReservations = reservationRepository.findAll();
        double totalRevenue = 0.0;

        for (Reservation res : allReservations) {


            if (res.getStatus() == ReservationStatus.CONFIRMED) {

                Optional<FarePolicy> policy = farePolicyRepository.findByRouteIdAndPassengerCategoryAndVehicleClass(
                        res.getRoute().getId(),
                        res.getPassengerCategory(),
                        res.getVehicleClass()
                );

                if (policy.isPresent()) {
                    double basePrice = policy.get().getPrice();
                    double discount = res.getPassengerCategory().getDiscountPercentage();
                    double seatPrice = basePrice * (1 - discount / 100.0);
                    // seat price takes into consideration the discount set in the enum for the passenger category
                    totalRevenue += (seatPrice * res.getSeatCount());
                } else {
                    System.err.println("No fare policy found for reservation ID: " + res.getId());
                }
            }
        }
        return totalRevenue;
    }
}