import { useState, useEffect, useCallback } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { loadStripe } from '@stripe/stripe-js';
import type { Stripe } from '@stripe/stripe-js';
import { Elements, PaymentElement, useStripe, useElements } from '@stripe/react-stripe-js';
import { useAuth } from '../../contexts/AuthContext';
import { seatApi, getSessionId } from '../../services/seatApi';
import { bookingApi } from '../../services/bookingApi';
import type { CreateReservationRequest } from '../../services/bookingApi';
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
    occupiedSeats,
    heldSeats,
    onSeatSelect,
    rows,
    prefix = '',
    deckLabel
}: {
    selectedSeats: string[],
    occupiedSeats: string[],
    heldSeats: string[],
    onSeatSelect: (seat: string) => void,
    rows: number[],
    prefix?: string,
    deckLabel?: string
}) => {
    const getSeatStatus = (seatId: string) => {
        if (occupiedSeats.includes(seatId)) return 'occupied';
        if (heldSeats.includes(seatId)) return 'held';
        if (selectedSeats.includes(seatId)) return 'selected';
        return 'available';
    };

    const handleSeatClick = (seatId: string) => {
        const status = getSeatStatus(seatId);
        if (status === 'occupied' || status === 'held') return; // Can't select occupied/held seats
        onSeatSelect(seatId);
    };

    return (
        <div className="bus-layout">
            {deckLabel && <div className="deck-label">{deckLabel}</div>}
            <div className="driver-area">{prefix ? 'Stairs' : 'Driver'}</div>
            {rows.map(row => (
                <div key={row} className="seat-row">
                    <div className="seat-pair left">
                        {['A', 'B'].map(col => {
                            const seatId = `${prefix}${row}${col}`;
                            const status = getSeatStatus(seatId);
                            return (
                                <button
                                    key={seatId}
                                    className={`seat ${status}`}
                                    onClick={() => handleSeatClick(seatId)}
                                    disabled={status === 'occupied' || status === 'held'}
                                    title={status === 'occupied' ? `Seat ${seatId} - Booked` :
                                           status === 'held' ? `Seat ${seatId} - Being reserved` :
                                           `Seat ${seatId}`}
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
                            const status = getSeatStatus(seatId);
                            return (
                                <button
                                    key={seatId}
                                    className={`seat ${status}`}
                                    onClick={() => handleSeatClick(seatId)}
                                    disabled={status === 'occupied' || status === 'held'}
                                    title={status === 'occupied' ? `Seat ${seatId} - Booked` :
                                           status === 'held' ? `Seat ${seatId} - Being reserved` :
                                           `Seat ${seatId}`}
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
    occupiedSeats,
    heldSeats,
    onSeatSelect,
    capacity,
    vehicleClass,
    loading
}: {
    selectedSeats: string[],
    occupiedSeats: string[],
    heldSeats: string[],
    onSeatSelect: (seat: string) => void,
    capacity: number,
    vehicleClass?: string,
    loading?: boolean
}) => {
    const isDoubleDecker = vehicleClass === 'DOUBLE_DECKER';

    // Double decker: 40 seats per floor (10 rows x 4 seats)
    // Regular bus: calculate based on capacity
    const seatsPerFloor = isDoubleDecker ? 40 : capacity;
    const rowsPerFloor = Math.ceil(seatsPerFloor / 4);
    const rows = Array.from({ length: rowsPerFloor }, (_, i) => i + 1);

    if (loading) {
        return (
            <div className="seat-map-container">
                <p style={{ textAlign: 'center', padding: '2rem' }}>Loading seat availability...</p>
            </div>
        );
    }

    return (
        <div className="seat-map-container">
            <div className="seat-map-legend">
                <div className="legend-item"><span className="seat available"></span> Available</div>
                <div className="legend-item"><span className="seat selected"></span> Selected</div>
                <div className="legend-item"><span className="seat occupied"></span> Booked</div>
                <div className="legend-item"><span className="seat held"></span> Reserved</div>
            </div>

            {isDoubleDecker ? (
                <div className="double-decker-layout">
                    <SingleDeck
                        selectedSeats={selectedSeats}
                        occupiedSeats={occupiedSeats}
                        heldSeats={heldSeats}
                        onSeatSelect={onSeatSelect}
                        rows={rows}
                        prefix="L"
                        deckLabel="Lower Deck (40 seats)"
                    />
                    <SingleDeck
                        selectedSeats={selectedSeats}
                        occupiedSeats={occupiedSeats}
                        heldSeats={heldSeats}
                        onSeatSelect={onSeatSelect}
                        rows={rows}
                        prefix="U"
                        deckLabel="Upper Deck (40 seats)"
                    />
                </div>
            ) : (
                <SingleDeck
                    selectedSeats={selectedSeats}
                    occupiedSeats={occupiedSeats}
                    heldSeats={heldSeats}
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
    const { user, isAuthenticated } = useAuth();
    const state = location.state as LocationState;

    useEffect(() => {
        if (!state) navigate('/');
    }, [state, navigate]);

    if (!state) return null;

    const { route, timetable, passengerBreakdown, isRoundTrip, returnRoute, returnTimetable, returnDate } = state;

    // Initialize passengers based on breakdown
    const [passengers, setPassengers] = useState<Passenger[]>([]);

    // Initialize passengers when component mounts or user changes
    useEffect(() => {
        const initialPassengers: Passenger[] = [];
        let idCounter = 0;

        // Add Adults
        for (let i = 0; i < (passengerBreakdown?.adult || 0); i++) {
            // Auto-populate first adult passenger with authenticated user data
            if (idCounter === 0 && isAuthenticated && user) {
                initialPassengers.push({
                    id: idCounter++,
                    type: 'Adult',
                    firstName: user.firstName || '',
                    lastName: user.lastName || ''
                });
            } else {
                initialPassengers.push({ id: idCounter++, type: 'Adult', firstName: '', lastName: '' });
            }
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
        if (initialPassengers.length === 0 && state?.passengers > 0) {
            for (let i = 0; i < state.passengers; i++) {
                // Auto-populate first passenger with authenticated user data
                if (i === 0 && isAuthenticated && user) {
                    initialPassengers.push({
                        id: i,
                        type: 'Adult',
                        firstName: user.firstName || '',
                        lastName: user.lastName || ''
                    });
                } else {
                    initialPassengers.push({ id: i, type: 'Adult', firstName: '', lastName: '' });
                }
            }
        }

        setPassengers(initialPassengers);
    }, [isAuthenticated, user, passengerBreakdown, state?.passengers]);

    const [luggageCount, setLuggageCount] = useState(0);
    const [seatSelectionOpen, setSeatSelectionOpen] = useState(false);
    const [selectedSeats, setSelectedSeats] = useState<string[]>([]);
    const [paymentSuccess, setPaymentSuccess] = useState(false);
    const [paymentError, setPaymentError] = useState<string | null>(null);

    // Seat availability state
    const [occupiedSeats, setOccupiedSeats] = useState<string[]>([]);
    const [heldSeats, setHeldSeats] = useState<string[]>([]);
    const [seatsLoading, setSeatsLoading] = useState(false);
    const [sessionId] = useState(() => getSessionId());

    // Currency state
    const [selectedCurrency, setSelectedCurrency] = useState<Currency>('RON');
    const [exchangeRates, setExchangeRates] = useState<ExchangeRates>({ EUR: 4.97, USD: 4.56, GBP: 5.78 });
    const [ratesLoading, setRatesLoading] = useState(true);

    // Helper to combine date and time string into ISO string
    const combineDateAndTime = (dateStr: string, timeStr: string) => {
        // dateStr is like "2023-10-25"
        // timeStr is ISO string "2023-10-25T10:30:00" or just time part
        
        // Create a date object from the time string (which has the correct time but potentially wrong date)
        const timeDate = new Date(timeStr);
        
        // Parse the selected date components
        const [year, month, day] = dateStr.split('-').map(Number);
        
        // Create a new date object based on the timeDate
        const newDate = new Date(timeDate);
        
        // Update the date components to match the selected date
        // Note: month is 0-indexed in JS Date
        newDate.setFullYear(year);
        newDate.setMonth(month - 1);
        newDate.setDate(day);
        
        return newDate.toISOString();
    };

    // Get departure time from timetable
    const getDepartureTimeISO = useCallback(() => {
        const originStop = timetable?.timetableStops?.find(s => s.station.id === route.originStation.id);
        if (!originStop?.departureTime) return null;
        
        // Combine the selected date (state.date) with the time from the timetable
        return combineDateAndTime(state.date, originStop.departureTime);
    }, [timetable, route, state.date]);

    // Fetch seat availability when seat selection opens
    useEffect(() => {
        const fetchSeatAvailability = async () => {
            if (!seatSelectionOpen || !route?.id) return;

            const departureTimeISO = getDepartureTimeISO();
            if (!departureTimeISO) return;

            setSeatsLoading(true);
            try {
                const availability = await seatApi.getOccupiedSeats(
                    route.id,
                    departureTimeISO,
                    sessionId
                );
                setOccupiedSeats(availability.occupiedSeats);
                setHeldSeats(availability.heldSeats);
            } catch (error) {
                console.error('Failed to fetch seat availability:', error);
            } finally {
                setSeatsLoading(false);
            }
        };

        fetchSeatAvailability();
        // Refresh every 30 seconds while seat selection is open
        const interval = seatSelectionOpen ? setInterval(fetchSeatAvailability, 30000) : undefined;
        return () => {
            if (interval) clearInterval(interval);
        };
    }, [seatSelectionOpen, route?.id, getDepartureTimeISO, sessionId]);

    // Release seat holds when leaving the page
    useEffect(() => {
        return () => {
            // Cleanup on unmount - release any held seats
            seatApi.releaseHolds(sessionId).catch(console.error);
        };
    }, [sessionId]);

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
                // Using fallback exchange rates
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

    const handleSeatSelect = async (seatId: string) => {
        const departureTimeISO = getDepartureTimeISO();
        if (!departureTimeISO) return;

        let newSelectedSeats: string[];
        if (selectedSeats.includes(seatId)) {
            // Deselecting a seat
            newSelectedSeats = selectedSeats.filter(s => s !== seatId);
        } else {
            if (selectedSeats.length < passengers.length) {
                newSelectedSeats = [...selectedSeats, seatId];
            } else {
                // Replace the first selected seat if max reached
                newSelectedSeats = [...selectedSeats.slice(1), seatId];
            }
        }

        // Optimistically update UI
        setSelectedSeats(newSelectedSeats);

        // Call API to hold/release seats
        if (newSelectedSeats.length > 0) {
            try {
                const response = await seatApi.holdSeats({
                    routeId: route.id,
                    departureTime: departureTimeISO,
                    seatNumbers: newSelectedSeats,
                    sessionId
                });

                if (!response.success) {
                    console.warn('Some seats could not be held:', response.failedSeats);
                    // Remove failed seats from selection
                    setSelectedSeats(response.heldSeats);
                }
            } catch (error) {
                console.error('Failed to hold seats:', error);
            }
        } else {
            // Release all holds if no seats selected
            try {
                await seatApi.releaseHolds(sessionId);
            } catch (error) {
                console.error('Failed to release seats:', error);
            }
        }
    };

    // Extract times from timetable
    const originStop = timetable.timetableStops.find(s => s.station.id === route.originStation.id);
    const destStop = timetable.timetableStops.find(s => s.station.id === route.destinationStation.id);
    
    const departureTime = originStop?.departureTime ? new Date(originStop.departureTime).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'}) : "--:--";
    const arrivalTime = destStop?.arrivalTime ? new Date(destStop.arrivalTime).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'}) : "--:--";

    const handlePaymentSuccess = async () => {
        try {
            // Get departure and arrival times from timetable
            const originStop = timetable.timetableStops.find(s => s.station.id === route.originStation.id);
            const destStop = timetable.timetableStops.find(s => s.station.id === route.destinationStation.id);

            if (!originStop?.departureTime || !destStop?.arrivalTime) {
                throw new Error('Missing trip time information');
            }

            // Map frontend passenger type to backend enum
            const passengerCategoryMap: Record<string, 'ADULT' | 'CHILD' | 'STUDENT'> = {
                'Adult': 'ADULT',
                'Child': 'CHILD',
                'Student': 'STUDENT'
            };

            // Map vehicle class
            const vehicleClassMap: Record<string, 'STANDARD' | 'COACH' | 'MINI_BUS' | 'DOUBLE_DECKER'> = {
                'STANDARD': 'STANDARD',
                'COACH': 'COACH',
                'MINI_BUS': 'MINI_BUS',
                'DOUBLE_DECKER': 'DOUBLE_DECKER'
            };

            // Build passenger names (comma-separated for multiple)
            const passengerNames = passengers
                .map(p => `${p.firstName} ${p.lastName}`.trim())
                .filter(name => name.length > 0)
                .join(', ') || 'Guest';

            // Calculate correct departure/arrival datetimes
            const departureDateTime = combineDateAndTime(state.date, originStop.departureTime);
            
            // For arrival, we need to handle overnight trips
            // If arrival time is earlier than departure time (in hours), it means next day
            const depTime = new Date(originStop.departureTime);
            const arrTime = new Date(destStop.arrivalTime);
            
            let arrivalDateStr = state.date;
            if (arrTime.getHours() < depTime.getHours() || 
               (arrTime.getHours() === depTime.getHours() && arrTime.getMinutes() < depTime.getMinutes())) {
                // Arrival is next day
                const nextDay = new Date(state.date);
                nextDay.setDate(nextDay.getDate() + 1);
                arrivalDateStr = nextDay.toISOString().split('T')[0];
            }
            
            const arrivalDateTime = combineDateAndTime(arrivalDateStr, destStop.arrivalTime);

            // Create reservation request for outbound trip
            const outboundRequest: CreateReservationRequest = {
                routeId: route.id,
                passengerName: passengerNames,
                seatCount: passengers.length,
                departureTime: departureDateTime,
                arrivalTime: arrivalDateTime,
                passengerCategory: passengerCategoryMap[passengers[0]?.type] || 'ADULT',
                vehicleClass: vehicleClassMap[route.vehicleClass || 'STANDARD'] || 'STANDARD',
                selectedSeats: selectedSeats.length > 0 ? selectedSeats : undefined,
                sessionId: sessionId,
                totalPrice: total,
                currency: selectedCurrency
            };

            await bookingApi.createReservation(outboundRequest);

            // Create return trip reservation if round-trip
            if (isRoundTrip && returnRoute && returnTimetable && returnDate) {
                const returnOriginStop = returnTimetable.timetableStops.find(
                    s => s.station.id === returnRoute.originStation.id
                );
                const returnDestStop = returnTimetable.timetableStops.find(
                    s => s.station.id === returnRoute.destinationStation.id
                );

                if (returnOriginStop?.departureTime && returnDestStop?.arrivalTime) {
                    const returnDepDateTime = combineDateAndTime(returnDate, returnOriginStop.departureTime);
                    
                    // Handle overnight return
                    const retDepTime = new Date(returnOriginStop.departureTime);
                    const retArrTime = new Date(returnDestStop.arrivalTime);
                    
                    let returnArrivalDateStr = returnDate;
                    if (retArrTime.getHours() < retDepTime.getHours() || 
                       (retArrTime.getHours() === retDepTime.getHours() && retArrTime.getMinutes() < retDepTime.getMinutes())) {
                        const nextDay = new Date(returnDate);
                        nextDay.setDate(nextDay.getDate() + 1);
                        returnArrivalDateStr = nextDay.toISOString().split('T')[0];
                    }
                    
                    const returnArrDateTime = combineDateAndTime(returnArrivalDateStr, returnDestStop.arrivalTime);

                    const returnRequest: CreateReservationRequest = {
                        routeId: returnRoute.id,
                        passengerName: passengerNames,
                        seatCount: passengers.length,
                        departureTime: returnDepDateTime,
                        arrivalTime: returnArrDateTime,
                        passengerCategory: passengerCategoryMap[passengers[0]?.type] || 'ADULT',
                        vehicleClass: vehicleClassMap[returnRoute.vehicleClass || 'STANDARD'] || 'STANDARD',
                        sessionId: sessionId,
                        totalPrice: 0, // Price already included in outbound total
                        currency: selectedCurrency
                    };

                    await bookingApi.createReservation(returnRequest);
                }
            }

            setPaymentSuccess(true);
        } catch (error) {
            console.error('Failed to create reservation:', error);
            const errorMessage = error instanceof Error ? error.message : 'Unknown error';
            setPaymentError(`Payment succeeded but failed to save booking: ${errorMessage}`);
        }
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
                                occupiedSeats={occupiedSeats}
                                heldSeats={heldSeats}
                                onSeatSelect={handleSeatSelect}
                                capacity={route.vehicleCapacity || 50}
                                vehicleClass={route.vehicleClass}
                                loading={seatsLoading}
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
