import { useState, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { loadStripe } from '@stripe/stripe-js';
import type { Stripe } from '@stripe/stripe-js';
import { Elements, PaymentElement, useStripe, useElements } from '@stripe/react-stripe-js';
import './BookingPage.css';

const API_BASE_URL = '/api';

// --- Types ---
interface RouteType {
    id: number;
    originStation: { id: number; name: string };
    destinationStation: { id: number; name: string };
    distance: number;
    durationMinutes: number;
    pricePerKm?: number;
    vehicleCapacity?: number;
    vehicleClass?: 'STANDARD' | 'COACH' | 'MINI_BUS' | 'DOUBLE_DECKER';
}

interface TimetableType {
    id: number;
    timetableStops: Array<{
        station: { id: number };
        arrivalTime: string;
        departureTime: string | null;
    }>;
}

interface LocationState {
    route: RouteType;
    timetable: TimetableType;
    date: string;
    passengers: number;
    passengerBreakdown: {
        adult: number;
        child: number;
        student: number;
    };
    // Round-trip fields
    isRoundTrip?: boolean;
    returnRoute?: RouteType;
    returnTimetable?: TimetableType;
    returnDate?: string;
}

interface Passenger {
    id: number;
    type: 'Adult' | 'Child' | 'Student';
    firstName: string;
    lastName: string;
    seatNumber?: string;
}

// --- Constants ---
const PRICE_PER_KM = 0.75; // RON per km
const LUGGAGE_PRICE = 25.00; // RON
const SEAT_RESERVATION_PRICE = 15.00; // RON
const SERVICE_FEE = 7.50; // RON

const PASSENGER_MULTIPLIERS = {
    Adult: 1,
    Child: 0.5, // 50% discount
    Student: 0.8 // 20% discount
};

// Supported currencies
type Currency = 'RON' | 'EUR' | 'USD' | 'GBP';

interface ExchangeRates {
    EUR: number;
    USD: number;
    GBP: number;
}

const CURRENCY_SYMBOLS: Record<Currency, string> = {
    RON: 'RON',
    EUR: 'EUR',
    USD: 'USD',
    GBP: 'GBP'
};

// --- Payment Form Component ---
const PaymentForm = ({
    amount,
    currency,
    onSuccess,
    onError
}: {
    amount: number;
    currency: string;
    onSuccess: () => void;
    onError: (error: string) => void;
}) => {
    const stripe = useStripe();
    const elements = useElements();
    const [isProcessing, setIsProcessing] = useState(false);
    const [errorMessage, setErrorMessage] = useState<string | null>(null);

    const handleSubmit = async (event: React.FormEvent) => {
        event.preventDefault();

        if (!stripe || !elements) {
            return;
        }

        setIsProcessing(true);
        setErrorMessage(null);

        const { error, paymentIntent } = await stripe.confirmPayment({
            elements,
            confirmParams: {
                return_url: window.location.origin + '/booking/success',
            },
            redirect: 'if_required',
        });

        if (error) {
            setErrorMessage(error.message || 'Payment failed');
            onError(error.message || 'Payment failed');
            setIsProcessing(false);
        } else if (paymentIntent && paymentIntent.status === 'succeeded') {
            onSuccess();
        } else {
            setIsProcessing(false);
        }
    };

    return (
        <form onSubmit={handleSubmit}>
            <div style={{ marginBottom: '1rem' }}>
                <PaymentElement />
            </div>
            {errorMessage && (
                <div className="payment-error" style={{ color: 'red', marginBottom: '1rem', fontSize: '0.9rem' }}>
                    {errorMessage}
                </div>
            )}
            <button
                className="pay-btn"
                disabled={!stripe || isProcessing}
                type="submit"
            >
                {isProcessing ? 'Processing...' : `Pay ${amount.toFixed(2)} ${currency}`}
            </button>
        </form>
    );
};

// --- Payment Wrapper Component ---
const PaymentSection = ({
    amount,
    currency,
    description,
    onSuccess,
    onError
}: {
    amount: number;
    currency: Currency;
    description: string;
    onSuccess: () => void;
    onError: (error: string) => void;
}) => {
    const [clientSecret, setClientSecret] = useState<string | null>(null);
    const [stripePromise, setStripePromise] = useState<Promise<Stripe | null> | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    // Map currency to Stripe currency codes
    const stripeCurrencyMap: Record<Currency, string> = {
        RON: 'ron',
        EUR: 'eur',
        USD: 'usd',
        GBP: 'gbp'
    };

    useEffect(() => {
        const initializePayment = async () => {
            try {
                // First, get the Stripe public key
                const configResponse = await fetch(`${API_BASE_URL}/payments/config`);
                if (!configResponse.ok) throw new Error('Failed to get Stripe config');
                const config = await configResponse.json();

                setStripePromise(loadStripe(config.publicKey));

                // Then create the payment intent with selected currency
                const paymentResponse = await fetch(`${API_BASE_URL}/payments/create-payment-intent`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                        amount: Math.round(amount * 100), // Convert to cents/bani
                        currency: stripeCurrencyMap[currency],
                        description: description
                    })
                });

                if (!paymentResponse.ok) throw new Error('Failed to create payment intent');
                const paymentData = await paymentResponse.json();

                setClientSecret(paymentData.clientSecret);
                setLoading(false);
            } catch (err) {
                console.error('Payment initialization error:', err);
                setError('Failed to initialize payment. Please try again.');
                setLoading(false);
            }
        };

        if (amount > 0) {
            initializePayment();
        }
    }, [amount, currency, description]);

    if (loading) {
        return (
            <div className="payment-loading" style={{ textAlign: 'center', padding: '2rem' }}>
                <p>Initializing payment...</p>
            </div>
        );
    }

    if (error) {
        return (
            <div className="payment-error-container" style={{ textAlign: 'center', padding: '2rem' }}>
                <p style={{ color: 'red' }}>{error}</p>
                <button
                    className="pay-btn"
                    onClick={() => window.location.reload()}
                    style={{ marginTop: '1rem' }}
                >
                    Retry
                </button>
            </div>
        );
    }

    if (!clientSecret || !stripePromise) {
        return (
            <div className="payment-error-container" style={{ textAlign: 'center', padding: '2rem' }}>
                <p>Unable to load payment form. Please refresh the page.</p>
            </div>
        );
    }

    const options = {
        clientSecret,
        appearance: {
            theme: 'stripe' as const,
            variables: {
                colorPrimary: '#6A0DAD',
            }
        }
    };

    return (
        <Elements stripe={stripePromise} options={options}>
            <PaymentForm
                amount={amount}
                currency={currency}
                onSuccess={onSuccess}
                onError={onError}
            />
        </Elements>
    );
};

// --- Single Deck Component ---
const SingleDeck = ({
    selectedSeats,
    onSeatSelect,
    rows,
    prefix = '',
    deckLabel
}: {
    selectedSeats: string[],
    onSeatSelect: (seat: string) => void,
    rows: number[],
    prefix?: string,
    deckLabel?: string
}) => {
    return (
        <div className="bus-layout">
            {deckLabel && <div className="deck-label">{deckLabel}</div>}
            <div className="driver-area">{prefix ? 'Stairs' : 'Driver'}</div>
            {rows.map(row => (
                <div key={row} className="seat-row">
                    <div className="seat-pair left">
                        {['A', 'B'].map(col => {
                            const seatId = `${prefix}${row}${col}`;
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
                            const seatId = `${prefix}${row}${col}`;
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
    );
};

// --- Seat Map Component ---
const SeatMap = ({
    selectedSeats,
    onSeatSelect,
    capacity,
    vehicleClass
}: {
    selectedSeats: string[],
    onSeatSelect: (seat: string) => void,
    capacity: number,
    vehicleClass?: string
}) => {
    const isDoubleDecker = vehicleClass === 'DOUBLE_DECKER';

    // Double decker: 40 seats per floor (10 rows x 4 seats)
    // Regular bus: calculate based on capacity
    const seatsPerFloor = isDoubleDecker ? 40 : capacity;
    const rowsPerFloor = Math.ceil(seatsPerFloor / 4);
    const rows = Array.from({ length: rowsPerFloor }, (_, i) => i + 1);

    return (
        <div className="seat-map-container">
            <div className="seat-map-legend">
                <div className="legend-item"><span className="seat available"></span> Available</div>
                <div className="legend-item"><span className="seat selected"></span> Selected</div>
                <div className="legend-item"><span className="seat occupied"></span> Occupied</div>
            </div>

            {isDoubleDecker ? (
                <div className="double-decker-layout">
                    <SingleDeck
                        selectedSeats={selectedSeats}
                        onSeatSelect={onSeatSelect}
                        rows={rows}
                        prefix="L"
                        deckLabel="Lower Deck (40 seats)"
                    />
                    <SingleDeck
                        selectedSeats={selectedSeats}
                        onSeatSelect={onSeatSelect}
                        rows={rows}
                        prefix="U"
                        deckLabel="Upper Deck (40 seats)"
                    />
                </div>
            ) : (
                <SingleDeck
                    selectedSeats={selectedSeats}
                    onSeatSelect={onSeatSelect}
                    rows={rows}
                />
            )}

            <p className="seat-info-text">
                Select a seat for {SEAT_RESERVATION_PRICE.toFixed(2)} RON or skip to get a random seat assigned for free at check-in.
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

    const { route, timetable, passengerBreakdown, isRoundTrip, returnRoute, returnTimetable, returnDate } = state;

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
    const [paymentSuccess, setPaymentSuccess] = useState(false);
    const [paymentError, setPaymentError] = useState<string | null>(null);

    // Currency state
    const [selectedCurrency, setSelectedCurrency] = useState<Currency>('RON');
    const [exchangeRates, setExchangeRates] = useState<ExchangeRates>({ EUR: 4.97, USD: 4.56, GBP: 5.78 });
    const [ratesLoading, setRatesLoading] = useState(true);

    // Fetch BNR exchange rates from backend
    useEffect(() => {
        const fetchBNRRates = async () => {
            try {
                const response = await fetch(`${API_BASE_URL}/exchange-rates`);
                if (!response.ok) throw new Error('Failed to fetch rates');

                const rates = await response.json();
                setExchangeRates({
                    EUR: rates.EUR || 4.97,
                    USD: rates.USD || 4.56,
                    GBP: rates.GBP || 5.78
                });
            } catch (error) {
                console.log('Using fallback exchange rates');
                setExchangeRates({ EUR: 4.97, USD: 4.56, GBP: 5.78 });
            } finally {
                setRatesLoading(false);
            }
        };

        fetchBNRRates();
    }, []);

    // Convert RON to selected currency
    const convertPrice = (ronAmount: number): number => {
        if (selectedCurrency === 'RON') return ronAmount;
        return ronAmount / exchangeRates[selectedCurrency];
    };

    // Format price with currency
    const formatPrice = (ronAmount: number): string => {
        const converted = convertPrice(ronAmount);
        return `${converted.toFixed(2)} ${CURRENCY_SYMBOLS[selectedCurrency]}`;
    };

    // Calculations
    const basePrice = (route.distance || 0) * (route.pricePerKm || PRICE_PER_KM);
    const returnBasePrice = isRoundTrip && returnRoute
        ? (returnRoute.distance || 0) * (returnRoute.pricePerKm || PRICE_PER_KM)
        : 0;

    const calculateTotal = () => {
        // Outbound tickets
        const outboundTicketsTotal = passengers.reduce((sum, p) => {
            return sum + (basePrice * PASSENGER_MULTIPLIERS[p.type]);
        }, 0);

        // Return tickets (if round-trip)
        const returnTicketsTotal = isRoundTrip ? passengers.reduce((sum, p) => {
            return sum + (returnBasePrice * PASSENGER_MULTIPLIERS[p.type]);
        }, 0) : 0;

        const extrasTotal = luggageCount * LUGGAGE_PRICE;
        const seatsTotal = selectedSeats.length * SEAT_RESERVATION_PRICE;

        // Service fee applied once for round-trip, not doubled
        return outboundTicketsTotal + returnTicketsTotal + extrasTotal + seatsTotal + SERVICE_FEE;
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

    const handlePaymentSuccess = () => {
        setPaymentSuccess(true);
        // TODO: Create reservation in backend
    };

    const handlePaymentError = (error: string) => {
        setPaymentError(error);
    };

    // Show success screen after payment
    if (paymentSuccess) {
        return (
            <div className="booking-page">
                <div className="booking-container" style={{ justifyContent: 'center' }}>
                    <div className="booking-card" style={{ textAlign: 'center', padding: '3rem' }}>
                        <div style={{ fontSize: '4rem', marginBottom: '1rem' }}>&#10003;</div>
                        <h2 style={{ color: '#22c55e', marginBottom: '1rem' }}>Payment Successful!</h2>
                        <p style={{ marginBottom: '2rem' }}>Your booking has been confirmed.</p>
                        <p><strong>From:</strong> {route.originStation?.name}</p>
                        <p><strong>To:</strong> {route.destinationStation?.name}</p>
                        <p><strong>Date:</strong> {state.date}</p>
                        <p><strong>Total Paid:</strong> {formatPrice(total)}</p>
                        <button
                            className="pay-btn"
                            onClick={() => navigate('/')}
                            style={{ marginTop: '2rem' }}
                        >
                            Back to Home
                        </button>
                    </div>
                </div>
            </div>
        );
    }

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
                                capacity={route.vehicleCapacity || 50}
                                vehicleClass={route.vehicleClass}
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
                        <PaymentSection
                            amount={convertPrice(total)}
                            currency={selectedCurrency}
                            description={isRoundTrip
                                ? `Round Trip: ${route.originStation?.name} ↔ ${route.destinationStation?.name}`
                                : `Booking: ${route.originStation?.name} → ${route.destinationStation?.name}`
                            }
                            onSuccess={handlePaymentSuccess}
                            onError={handlePaymentError}
                        />
                        {paymentError && (
                            <div className="payment-error-message" style={{ color: 'red', marginTop: '1rem', textAlign: 'center' }}>
                                {paymentError}
                            </div>
                        )}
                    </div>
                </div>

                {/* RIGHT COLUMN - SUMMARY */}
                <div className="booking-right-column">
                    <div className="summary-card">
                        <div className="summary-header">
                            {isRoundTrip ? 'Round Trip Booking' : 'Your Booking'}
                        </div>

                        {/* Currency Selector */}
                        <div className="currency-selector">
                            <label className="currency-label">Currency:</label>
                            <select
                                className="currency-select"
                                value={selectedCurrency}
                                onChange={(e) => setSelectedCurrency(e.target.value as Currency)}
                            >
                                <option value="RON">RON (Lei)</option>
                                <option value="EUR">EUR</option>
                                <option value="USD">USD</option>
                                <option value="GBP">GBP</option>
                            </select>
                            {!ratesLoading && selectedCurrency !== 'RON' && (
                                <span className="exchange-rate-info">
                                    1 {selectedCurrency} = {exchangeRates[selectedCurrency].toFixed(4)} RON (BNR)
                                </span>
                            )}
                        </div>

                        {/* Outbound Journey */}
                        <div className="trip-section">
                            {isRoundTrip && <div className="trip-label">Outbound - {state.date}</div>}
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
                        </div>

                        {/* Return Journey (if round-trip) */}
                        {isRoundTrip && returnRoute && returnTimetable && (
                            <div className="trip-section trip-section-return">
                                <div className="trip-label">Return - {returnDate}</div>
                                <div className="trip-visual">
                                    <div className="trip-point">
                                        <div className="time">
                                            {(() => {
                                                const returnOriginStop = returnTimetable.timetableStops.find(s => s.station.id === returnRoute.originStation.id);
                                                return returnOriginStop?.departureTime ? new Date(returnOriginStop.departureTime).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'}) : "--:--";
                                            })()}
                                        </div>
                                        <div className="timeline-marker">
                                            <div className="dot"></div>
                                            <div className="line"></div>
                                        </div>
                                        <div className="station-name">{returnRoute.originStation?.name || "Origin"}</div>
                                    </div>
                                    <div className="trip-point">
                                        <div className="time">
                                            {(() => {
                                                const returnDestStop = returnTimetable.timetableStops.find(s => s.station.id === returnRoute.destinationStation.id);
                                                return returnDestStop?.arrivalTime ? new Date(returnDestStop.arrivalTime).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'}) : "--:--";
                                            })()}
                                        </div>
                                        <div className="timeline-marker">
                                            <div className="dot"></div>
                                        </div>
                                        <div className="station-name">{returnRoute.destinationStation?.name || "Destination"}</div>
                                    </div>
                                </div>
                            </div>
                        )}

                        <div className="price-breakdown">
                            {/* Outbound tickets */}
                            <div className="price-section-label">
                                {isRoundTrip ? 'Outbound' : 'Tickets'}
                            </div>
                            {passengers.map((p, i) => (
                                <div key={`out-${i}`} className="price-row">
                                    <span>1 {p.type}</span>
                                    <span>{formatPrice(basePrice * PASSENGER_MULTIPLIERS[p.type])}</span>
                                </div>
                            ))}

                            {/* Return tickets */}
                            {isRoundTrip && (
                                <>
                                    <div className="price-section-label price-section-label-return">Return</div>
                                    {passengers.map((p, i) => (
                                        <div key={`ret-${i}`} className="price-row">
                                            <span>1 {p.type}</span>
                                            <span>{formatPrice(returnBasePrice * PASSENGER_MULTIPLIERS[p.type])}</span>
                                        </div>
                                    ))}
                                </>
                            )}

                            {luggageCount > 0 && (
                                <div className="price-row">
                                    <span>{luggageCount} x Luggage</span>
                                    <span>{formatPrice(luggageCount * LUGGAGE_PRICE)}</span>
                                </div>
                            )}

                            {selectedSeats.length > 0 && (
                                <div className="price-row">
                                    <span>{selectedSeats.length} x Seat Reservation</span>
                                    <span>{formatPrice(selectedSeats.length * SEAT_RESERVATION_PRICE)}</span>
                                </div>
                            )}

                            <div className="price-row">
                                <span>Service Fee</span>
                                <span>{formatPrice(SERVICE_FEE)}</span>
                            </div>

                            <div className="price-row total">
                                <span>Total</span>
                                <span>{formatPrice(total)}</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}
