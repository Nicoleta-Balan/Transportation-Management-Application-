package multitier.trans.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * Temporary seat hold to prevent race conditions during booking.
 * Holds expire after 15 minutes if not converted to a confirmed reservation.
 */
@Entity
@Table(name = "seat_holds", indexes = {
    @Index(name = "idx_seat_hold_route_departure", columnList = "route_id, departure_time"),
    @Index(name = "idx_seat_hold_expiry", columnList = "expires_at"),
    @Index(name = "idx_seat_hold_session", columnList = "session_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatHold {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Route cannot be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @NotNull(message = "Departure time cannot be null")
    @Column(name = "departure_time", nullable = false)
    private LocalDateTime departureTime;

    @NotBlank(message = "Seat number cannot be blank")
    @Column(name = "seat_number", nullable = false, length = 10)
    private String seatNumber;

    // Session ID to identify the user's booking session (can be JWT token hash or UUID)
    @NotBlank(message = "Session ID cannot be blank")
    @Column(name = "session_id", nullable = false, length = 100)
    private String sessionId;

    // Optional: Link to user if authenticated
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @NotNull(message = "Created time cannot be null")
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @NotNull(message = "Expiry time cannot be null")
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.expiresAt == null) {
            // Default expiry: 15 minutes from creation
            this.expiresAt = this.createdAt.plusMinutes(15);
        }
    }

    /**
     * Check if this hold has expired
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }
}
