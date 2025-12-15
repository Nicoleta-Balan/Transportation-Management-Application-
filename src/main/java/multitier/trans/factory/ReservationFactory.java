package multitier.trans.factory;

import multitier.trans.dto.CreateReservationRequest;
import multitier.trans.model.Reservation;
import multitier.trans.model.Route;
import multitier.trans.model.TripTimeDetails;
import multitier.trans.model.enums.ReservationStatus;
import multitier.trans.repository.RouteRepository;
import multitier.trans.utils.RepositoryUtils;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReservationFactory {

    private final RouteRepository routeRepository;

    public Reservation createReservation(CreateReservationRequest request) {
        // 1. Find the Route
        Route route = RepositoryUtils.findByIdOrThrow(
            routeRepository.findById(request.getRouteId()),
            "Route",
            request.getRouteId()
        );

        // 2. Create the Value Object
        TripTimeDetails tripDetails = new TripTimeDetails(
                request.getDepartureTime(),
                request.getArrivalTime()
        );

        // 3. Create the new Reservation entity and map fields manually
        Reservation newReservation = new Reservation();
        newReservation.setPassengerName(request.getPassengerName());
        newReservation.setSeatCount(request.getSeatCount());
        newReservation.setPassengerCategory(request.getPassengerCategory());
        newReservation.setVehicleClass(request.getVehicleClass());
        newReservation.setStatus(ReservationStatus.PENDING); // Default status
        newReservation.setRoute(route);
        newReservation.setTripDetails(tripDetails);

        return newReservation;
    }
}

