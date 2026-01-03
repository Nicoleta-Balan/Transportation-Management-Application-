import { useState, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { loadStripe } from '@stripe/stripe-js';
import { Elements, PaymentElement, useStripe, useElements } from '@stripe/react-stripe-js';
import './BookingPage.css';

// --- Types ---
interface LocationState {
    route: {
        id: number;
        originStation: { id: number; name: string };
        destinationStation: { id: number; name: string };
        distance: number;
        durationMinutes: number;
        pricePerKm?: number;
        vehicleCapacity?: number; // Added vehicle capacity
    };
    timetable: {
        id: number;
        timetableStops: Array<{
            station: { id: number };
            arrivalTime: string;
            departureTime: string | null;
        }>;
    };
    date: string;
    passengers: number;
    passengerBreakdown: {
        adult: number;
        child: number;
        student: number;
    };
}

interface Passenger {
    id: number;
    type: 'Adult' | 'Child' | 'Student';
    firstName: string;
    lastName: string;
    seatNumber?: string;
}

// --- Constants ---
const STRIPE_PUBLIC_KEY = "pk_test_placeholder"; // REPLACE WITH YOUR KEY
const stripePromise = loadStripe(STRIPE_PUBLIC_KEY);

const PRICE_PER_KM = 0.15;
const LUGGAGE_PRICE = 5.00;
const SEAT_RESERVATION_PRICE = 3.00;
const SERVICE_FEE = 1.50;

const PASSENGER_MULTIPLIERS = {
    Adult: 1,
    Child: 0.5, // 50% discount
    Student: 0.8 // 20% discount
};

// --- Payment Form Component ---
const PaymentForm = ({ amount }: { amount: number }) => {
    const stripe = useStripe();
    const elements = useElements();

    const handleSubmit = async (event: React.FormEvent) => {
        event.preventDefault();
        if (!stripe || !elements) return;
        console.log("Processing payment for:", amount);
        alert("Payment integration would trigger here.");
    };

    return (
        <form onSubmit={handleSubmit}>
            <div style={{ marginBottom: '1rem' }}>
                <PaymentElement />
            </div>
            <button className="pay-btn" disabled={!stripe}>
                Pay ${amount.toFixed(2)}
            </button>
        </form>
    );
};

// --- Seat Map Component ---
const SeatMap = ({ 
    selectedSeats, 
    onSeatSelect,
    capacity
}: { 
    selectedSeats: string[], 
    onSeatSelect: (seat: string) => void,
    capacity: number
}) => {
    // Calculate rows based on capacity (4 seats per row)
    const totalRows = Math.ceil(capacity / 4);
    const rows = Array.from({ length: totalRows }, (_, i) => i + 1);
    
    return (
        <div className="seat-map-container">
            <div className="seat-map-legend">
                <div className="legend-item"><span className="seat available"></span> Available</div>
                <div className="legend-item"><span className="seat selected"></span> Selected</div>
                <div className="legend-item"><span className="seat occupied"></span> Occupied</div>
            </div>
            
            <div className="bus-layout">
                <div className="driver-area">Driver</div>
                {rows.map(row => (
                    <div key={row} className="seat-row">
                        <div className="seat-pair left">
                            {['A', 'B'].map(col => {
                                const seatId = `${row}${col}`;
                                // Check if seat is within capacity (e.g. last row might not be full)
                                const seatIndex = (row - 1) * 4 + (col === 'A' ? 0 : 1);
                                if (seatIndex >= capacity) return <div key={seatId} className="seat-placeholder"></div>;
                                
                                const isSelected = selectedSeats.includes(seatId);
                                return (
                                    <button
                                        key={seatId}
                                        className={`seat ${isSelected ? 'selected' : 'available'}`}
                                        onClick={() => onSeatSelect(seatId)}
                                        title={`Seat ${seatId}`}
                                    >
                                        {seatId}
                                    </button>
                                );
                            })}
                        </div>
                        <div className="aisle">{row}</div>
                        <div className="seat-pair right">
                            {['C', 'D'].map(col => {
                                const seatId = `${row}${col}`;
                                // Check if seat is within capacity
                                const seatIndex = (row - 1) * 4 + (col === 'C' ? 2 : 3);
                                if (seatIndex >= capacity) return <div key={seatId} className="seat-placeholder"></div>;

                                const isSelected = selectedSeats.includes(seatId);
                                return (
                                    <button
                                        key={seatId}
                                        className={`seat ${isSelected ? 'selected' : 'available'}`}
                                        onClick={() => onSeatSelect(seatId)}
                                        title={`Seat ${seatId}`}
                                    >
                                        {seatId}
                                    </button>
                                );
                            })}
                        </div>
                    </div>
                ))}
            </div>
            <p className="seat-info-text">
                Select a seat for €{SEAT_RESERVATION_PRICE.toFixed(2)} or skip to get a random seat assigned for free at check-in.
            </p>
        </div>
    );
};

// --- Main Page Component ---
export default function BookingPage() {
    const location = useLocation();
    const navigate = useNavigate();
    const state = location.state as LocationState;

    useEffect(() => {
        if (!state) navigate('/');
    }, [state, navigate]);

    if (!state) return null;

    const { route, timetable, passengerBreakdown } = state;

    // Initialize passengers based on breakdown
    const [passengers, setPassengers] = useState<Passenger[]>(() => {
        const initialPassengers: Passenger[] = [];
        let idCounter = 0;
        
        // Add Adults
        for (let i = 0; i < (passengerBreakdown?.adult || 0); i++) {
            initialPassengers.push({ id: idCounter++, type: 'Adult', firstName: '', lastName: '' });
        }
        // Add Children
        for (let i = 0; i < (passengerBreakdown?.child || 0); i++) {
            initialPassengers.push({ id: idCounter++, type: 'Child', firstName: '', lastName: '' });
        }
        // Add Students
        for (let i = 0; i < (passengerBreakdown?.student || 0); i++) {
            initialPassengers.push({ id: idCounter++, type: 'Student', firstName: '', lastName: '' });
        }
        
        // Fallback if no breakdown provided (legacy support)
        if (initialPassengers.length === 0 && state.passengers > 0) {
            for (let i = 0; i < state.passengers; i++) {
                initialPassengers.push({ id: i, type: 'Adult', firstName: '', lastName: '' });
            }
        }
        
        return initialPassengers;
    });

    const [luggageCount, setLuggageCount] = useState(0);
    const [seatSelectionOpen, setSeatSelectionOpen] = useState(false);
    const [selectedSeats, setSelectedSeats] = useState<string[]>([]);

    // Calculations
    const basePrice = (route.distance || 0) * (route.pricePerKm || PRICE_PER_KM);

    const calculateTotal = () => {
        const ticketsTotal = passengers.reduce((sum, p) => {
            return sum + (basePrice * PASSENGER_MULTIPLIERS[p.type]);
        }, 0);
        
        const extrasTotal = luggageCount * LUGGAGE_PRICE;
        const seatsTotal = selectedSeats.length * SEAT_RESERVATION_PRICE;
        
        return ticketsTotal + extrasTotal + seatsTotal + SERVICE_FEE;
    };

    const total = calculateTotal();

    // Handlers
    const handlePassengerChange = (id: number, field: keyof Passenger, value: string) => {
        setPassengers(prev => prev.map(p => 
            p.id === id ? { ...p, [field]: value } : p
        ));
    };

    const handleSeatSelect = (seatId: string) => {
        if (selectedSeats.includes(seatId)) {
            setSelectedSeats(prev => prev.filter(s => s !== seatId));
        } else {
            if (selectedSeats.length < passengers.length) {
                setSelectedSeats(prev => [...prev, seatId]);
            } else {
                // Replace the last selected seat if max reached (simple logic)
                // Or alert user. Here we'll just ignore or could replace first.
                // Let's replace the first one to keep it simple for user to change mind
                setSelectedSeats(prev => [...prev.slice(1), seatId]);
            }
        }
    };

    // Extract times from timetable
    const originStop = timetable.timetableStops.find(s => s.station.id === route.originStation.id);
    const destStop = timetable.timetableStops.find(s => s.station.id === route.destinationStation.id);
    
    const departureTime = originStop?.departureTime ? new Date(originStop.departureTime).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'}) : "--:--";
    const arrivalTime = destStop?.arrivalTime ? new Date(destStop.arrivalTime).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'}) : "--:--";


    const options = {
        mode: 'payment' as const,
        amount: Math.round(total * 100),
        currency: 'usd',
    };

    return (
        <div className="booking-page">
            <header className="booking-header">
                <button className="back-btn" onClick={() => navigate(-1)}>
                    ← Back to search results
                </button>
            </header>

            <div className="booking-container">
                <div className="booking-left-column">
                    
                    {/* 1. Passengers */}
                    <div className="booking-card">
                        <div className="card-header">
                            <div className="step-badge">1</div>
                            <h2>Passengers</h2>
                        </div>
                        
                        {passengers.map((p, index) => (
                            <div key={p.id} className="passenger-group">
                                <label className="passenger-type-label">
                                    Passenger {index + 1} ({p.type})
                                </label>
                                <div className="input-row">
                                    <div className="input-wrapper">
                                        <label htmlFor={`fname-${p.id}`}>First Name</label>
                                        <input 
                                            id={`fname-${p.id}`}
                                            type="text" 
                                            className="booking-input"
                                            value={p.firstName}
                                            onChange={(e) => handlePassengerChange(p.id, 'firstName', e.target.value)}
                                            placeholder="e.g. John"
                                        />
                                    </div>
                                    <div className="input-wrapper">
                                        <label htmlFor={`lname-${p.id}`}>Last Name</label>
                                        <input 
                                            id={`lname-${p.id}`}
                                            type="text" 
                                            className="booking-input"
                                            value={p.lastName}
                                            onChange={(e) => handlePassengerChange(p.id, 'lastName', e.target.value)}
                                            placeholder="e.g. Doe"
                                        />
                                    </div>
                                </div>
                            </div>
                        ))}
                    </div>

                    {/* 2. Seat Reservation */}
                    <div className="booking-card">
                        <div className="card-header">
                            <div className="step-badge">2</div>
                            <h2>Seat Reservation</h2>
                        </div>
                        <div 
                            className="seat-selector-row"
                            onClick={() => setSeatSelectionOpen(!seatSelectionOpen)}
                        >
                            <div className="seat-content">
                                <span className="seat-icon">💺</span>
                                <span>
                                    {selectedSeats.length > 0 
                                        ? `${selectedSeats.length} seat(s) selected: ${selectedSeats.join(', ')}` 
                                        : "Select your seat"}
                                </span>
                            </div>
                            <span className="chevron">{seatSelectionOpen ? '▲' : '▼'}</span>
                        </div>
                        {seatSelectionOpen && (
                            <SeatMap 
                                selectedSeats={selectedSeats} 
                                onSeatSelect={handleSeatSelect}
                                capacity={route.vehicleCapacity || 50} // Use route capacity or default to 50
                            />
                        )}
                    </div>

                    {/* 3. Extras */}
                    <div className="booking-card">
                        <div className="card-header">
                            <div className="step-badge">3</div>
                            <h2>Extras</h2>
                        </div>
                        <div className="extra-item">
                            <div className="extra-info">
                                <h3>Additional Luggage</h3>
                                <p>Max. 20kg (70x50x30 cm)</p>
                            </div>
                            <div className="counter-control">
                                <button 
                                    className="counter-btn"
                                    onClick={() => setLuggageCount(Math.max(0, luggageCount - 1))}
                                >-</button>
                                <span>{luggageCount}</span>
                                <button 
                                    className="counter-btn"
                                    onClick={() => setLuggageCount(luggageCount + 1)}
                                >+</button>
                            </div>
                        </div>
                    </div>

                    {/* Payment */}
                    <div className="booking-card">
                        <div className="card-header">
                            <div className="step-badge">4</div>
                            <h2>Payment</h2>
                        </div>
                        <Elements stripe={stripePromise} options={options}>
                            <PaymentForm amount={total} />
                        </Elements>
                    </div>
                </div>

                {/* RIGHT COLUMN - SUMMARY */}
                <div className="booking-right-column">
                    <div className="summary-card">
                        <div className="summary-header">Your Booking</div>
                        
                        <div className="trip-visual">
                            <div className="trip-point">
                                <div className="time">{departureTime}</div>
                                <div className="timeline-marker">
                                    <div className="dot"></div>
                                    <div className="line"></div>
                                </div>
                                <div className="station-name">{route.originStation?.name || "Origin"}</div>
                            </div>
                            <div className="trip-point">
                                <div className="time">{arrivalTime}</div>
                                <div className="timeline-marker">
                                    <div className="dot"></div>
                                </div>
                                <div className="station-name">{route.destinationStation?.name || "Destination"}</div>
                            </div>
                        </div>

                        <div className="price-breakdown">
                            {passengers.map((p, i) => (
                                <div key={i} className="price-row">
                                    <span>1 {p.type}</span>
                                    <span>${(basePrice * PASSENGER_MULTIPLIERS[p.type]).toFixed(2)}</span>
                                </div>
                            ))}
                            
                            {luggageCount > 0 && (
                                <div className="price-row">
                                    <span>{luggageCount} x Luggage</span>
                                    <span>${(luggageCount * LUGGAGE_PRICE).toFixed(2)}</span>
                                </div>
                            )}
                            
                            {selectedSeats.length > 0 && (
                                <div className="price-row">
                                    <span>{selectedSeats.length} x Seat Reservation</span>
                                    <span>${(selectedSeats.length * SEAT_RESERVATION_PRICE).toFixed(2)}</span>
                                </div>
                            )}

                            <div className="price-row">
                                <span>Service Fee</span>
                                <span>${SERVICE_FEE.toFixed(2)}</span>
                            </div>

                            <div className="price-row total">
                                <span>Total</span>
                                <span>${total.toFixed(2)}</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}
