package multitier.trans.service;

import multitier.trans.dto.FareCalculationResponse;
import multitier.trans.model.enums.PassengerCategory;
import multitier.trans.model.enums.VehicleClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Service implementation for fare calculation operations.
 * Replaces database function calculate_reservation_fare().
 * 
 * This service combines:
 * - FarePolicyService to get base price
 * - VatRateService to get VAT rate
 * - Business logic to calculate total fare
 */
@Service
@Transactional(readOnly = true)
public class FareCalculationServiceImpl implements FareCalculationService {

    private final FarePolicyService farePolicyService;
    private final VatRateService vatRateService;

    @Autowired
    public FareCalculationServiceImpl(FarePolicyService farePolicyService, 
                                     VatRateService vatRateService) {
        this.farePolicyService = farePolicyService;
        this.vatRateService = vatRateService;
    }

    @Override
    public FareCalculationResponse calculateFare(Long routeId, PassengerCategory passengerCategory,
                                                 VehicleClass vehicleClass, Integer seatCount,
                                                 LocalDateTime departureTime) {
        // Get base price per seat from fare policy
        BigDecimal basePricePerSeat = farePolicyService.getActiveFarePolicyPrice(
                routeId, passengerCategory, vehicleClass, departureTime);

        // Calculate base fare (price per seat * number of seats)
        BigDecimal baseFare = basePricePerSeat
                .multiply(BigDecimal.valueOf(seatCount))
                .setScale(2, RoundingMode.HALF_UP);

        // Get VAT rate for the departure date
        BigDecimal vatRate = vatRateService.getVatRateForDate(departureTime);

        // Calculate VAT amount
        BigDecimal vatAmount = baseFare
                .multiply(vatRate)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        // Calculate total fare (base + VAT)
        BigDecimal totalFare = baseFare.add(vatAmount).setScale(2, RoundingMode.HALF_UP);

        return new FareCalculationResponse(baseFare, vatAmount, totalFare, vatRate);
    }
}

