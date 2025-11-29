package multitier.trans.controllers;

import jakarta.validation.Valid;
import multitier.trans.dto.CreateReservationRequest;
import multitier.trans.dto.ReservationResponse;
import multitier.trans.model.Reservation;
import multitier.trans.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;


@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    @Autowired
    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }


    @PostMapping
    public ResponseEntity<Reservation> createReservation(@Valid @RequestBody CreateReservationRequest request) {
        Reservation newReservation = reservationService.createReservation(request);
        return new ResponseEntity<>(newReservation, HttpStatus.CREATED); // 201 Created
    }


    @PutMapping("/{id}/cancel")
    public ResponseEntity<Reservation> cancelReservation(@PathVariable Long id) {
        Reservation cancelledReservation = reservationService.cancelReservation(id);
        return ResponseEntity.ok(cancelledReservation); // 200 OK
    }


    @GetMapping
    public List<ReservationResponse> getAllReservations() {
        return reservationService.getAllReservations();
    }


    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponse> getReservationById(@PathVariable Long id) {
        return reservationService.getReservationById(id)
                .map(ResponseEntity::ok) // 200 OK if found
                .orElse(ResponseEntity.notFound().build()); // 404 Not Found if not
    }
}