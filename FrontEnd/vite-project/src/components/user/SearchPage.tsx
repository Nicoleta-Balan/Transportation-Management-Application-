import { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { useStations } from '../../hooks/useStations';
import { timetableApi } from '../../services/timetableApi';
import type { Timetable } from '../../types/Timetable';
import './SearchPage.css';

export default function SearchPage() {
    const navigate = useNavigate();
    const { stations } = useStations();

    // Search State
    const [fromStationId, setFromStationId] = useState<string>('');
    const [toStationId, setToStationId] = useState<string>('');
    const [date, setDate] = useState('');
    const [tripType, setTripType] = useState<'oneWay' | 'roundTrip'>('oneWay');
    
    // Passenger State
    const [passengers, setPassengers] = useState({
        adult: 1,
        child: 0,
        bike: 0
    });
    const [isPassengerPopoverOpen, setIsPassengerPopoverOpen] = useState(false);
    const passengerPopoverRef = useRef<HTMLDivElement>(null);

    // Results State
    const [searchResults, setSearchResults] = useState<Timetable[]>([]);
    const [searched, setSearched] = useState(false);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    // Set default date to today
    useEffect(() => {
        const today = new Date().toISOString().split('T')[0];
        setDate(today);
    }, []);

    // Close popover when clicking outside
    useEffect(() => {
        function handleClickOutside(event: MouseEvent) {
            if (passengerPopoverRef.current && !passengerPopoverRef.current.contains(event.target as Node)) {
                setIsPassengerPopoverOpen(false);
            }
        }
        document.addEventListener("mousedown", handleClickOutside);
        return () => document.removeEventListener("mousedown", handleClickOutside);
    }, []);

    const handleSearch = async (e: React.FormEvent) => {
        e.preventDefault();

        if (!fromStationId || !toStationId || !date) {
            setError('Please fill in all required fields');
            return;
        }

        if (fromStationId === toStationId) {
            setError('Origin and destination cannot be the same');
            return;
        }

        setLoading(true);
        setError(null);
        setSearched(true);

        try {
            const results = await timetableApi.searchTimetables(
                parseInt(fromStationId),
                parseInt(toStationId),
                date
            );
            setSearchResults(results || []);
        } catch (err: unknown) {
            console.error('Search failed:', err);
            const errorMessage = err instanceof Error ? err.message : 'Unknown error';
            setError(`Failed to search: ${errorMessage}`);
            setSearchResults([]);
        } finally {
            setLoading(false);
        }
    };

    const handleBookClick = (timetable: Timetable) => {
        const totalPassengers = passengers.adult + passengers.child; // Bikes don't count as seats usually
        
        navigate('/booking', {
            state: {
                route: timetable.route,
                timetable: timetable,
                date: date,
                passengers: totalPassengers,
                passengerBreakdown: {
                    adult: passengers.adult,
                    child: passengers.child,
                    student: 0 // Mapping bike to student or handling separately? For now 0.
                }
            }
        });
    };

    const handleSwapStations = () => {
        const temp = fromStationId;
        setFromStationId(toStationId);
        setToStationId(temp);
    };

    const updatePassenger = (type: 'adult' | 'child' | 'bike', delta: number) => {
        setPassengers(prev => {
            const newValue = prev[type] + delta;
            // Constraints: Min 1 Adult, Min 0 others
            if (type === 'adult' && newValue < 1) return prev;
            if (newValue < 0) return prev;
            return { ...prev, [type]: newValue };
        });
    };

    const getPassengerSummary = () => {
        const parts = [];
        if (passengers.adult > 0) parts.push(`${passengers.adult} Adult${passengers.adult > 1 ? 's' : ''}`);
        if (passengers.child > 0) parts.push(`${passengers.child} Child${passengers.child > 1 ? 'ren' : ''}`);
        if (passengers.bike > 0) parts.push(`${passengers.bike} Bike${passengers.bike > 1 ? 's' : ''}`);
        return parts.join(', ');
    };

    // Filter available stations for "To" dropdown
    const availableToStations = stations.filter(s => s.id.toString() !== fromStationId);

    const formatDuration = (minutes: number) => {
        if (!minutes) return '0h 0m';
        const hrs = Math.floor(minutes / 60);
        const mins = minutes % 60;
        return `${hrs}h ${mins}m`;
    };

    const getDayName = (dateStr: string) => {
        const date = new Date(dateStr);
        return date.toLocaleDateString('en-US', { weekday: 'long' });
    };

    return (
        <div className="search-page">
            <header className="search-header">
                <div className="logo">BusBooking</div>
                <button className="admin-btn" onClick={() => navigate('/admin')}>
                    Admin Login
                </button>
            </header>

            <div className={`hero-section ${searched ? 'searched' : ''}`}>
                <div className="search-widget-container">
                    
                    {/* Top Row: Trip Type */}
                    <div className="trip-type-toggles">
                        <label className={`toggle-btn ${tripType === 'oneWay' ? 'active' : ''}`}>
                            <input 
                                type="radio" 
                                name="tripType" 
                                checked={tripType === 'oneWay'} 
                                onChange={() => setTripType('oneWay')}
                            />
                            One Way
                        </label>
                        <label className={`toggle-btn ${tripType === 'roundTrip' ? 'active' : ''}`}>
                            <input 
                                type="radio" 
                                name="tripType" 
                                checked={tripType === 'roundTrip'} 
                                onChange={() => setTripType('roundTrip')}
                            />
                            Round Trip
                        </label>
                    </div>

                    {/* Main Search Row */}
                    <form onSubmit={handleSearch} className="search-bar-row">
                        
                        {/* From */}
                        <div className="search-field-group">
                            <label>From</label>
                            <select
                                value={fromStationId}
                                onChange={(e) => {
                                    setFromStationId(e.target.value);
                                    if (e.target.value === toStationId) setToStationId('');
                                }}
                                required
                            >
                                <option value="">Select Origin</option>
                                {stations.map(s => (
                                    <option key={s.id} value={s.id}>{s.name}</option>
                                ))}
                            </select>
                        </div>

                        {/* Swap Button */}
                        <button type="button" className="swap-btn" onClick={handleSwapStations} title="Swap stations">
                            ⇄
                        </button>

                        {/* To */}
                        <div className="search-field-group">
                            <label>To</label>
                            <select
                                value={toStationId}
                                onChange={(e) => setToStationId(e.target.value)}
                                required
                            >
                                <option value="">Select Destination</option>
                                {availableToStations.map(s => (
                                    <option key={s.id} value={s.id}>{s.name}</option>
                                ))}
                            </select>
                        </div>

                        {/* Departure Date */}
                        <div className="search-field-group date-field">
                            <label>Departure</label>
                            <input 
                                type="date" 
                                value={date}
                                min={new Date().toISOString().split('T')[0]}
                                onChange={(e) => setDate(e.target.value)}
                                required
                            />
                        </div>

                        {/* Return Date (Optional/Disabled for One Way) */}
                        {tripType === 'roundTrip' && (
                            <div className="search-field-group date-field">
                                <label>Return</label>
                                <input type="date" disabled placeholder="Optional" />
                            </div>
                        )}

                        {/* Passengers Dropdown */}
                        <div className="search-field-group passenger-field" ref={passengerPopoverRef}>
                            <label>Passengers</label>
                            <div 
                                className="passenger-trigger"
                                onClick={() => setIsPassengerPopoverOpen(!isPassengerPopoverOpen)}
                            >
                                {getPassengerSummary()}
                            </div>

                            {isPassengerPopoverOpen && (
                                <div className="passenger-popover">
                                    {/* Adults */}
                                    <div className="popover-row">
                                        <div className="popover-label">
                                            <span>Adult</span>
                                        </div>
                                        <div className="popover-counter">
                                            <button 
                                                type="button" 
                                                disabled={passengers.adult <= 1}
                                                onClick={() => updatePassenger('adult', -1)}
                                            >−</button>
                                            <span>{passengers.adult}</span>
                                            <button 
                                                type="button" 
                                                onClick={() => updatePassenger('adult', 1)}
                                            >+</button>
                                        </div>
                                    </div>

                                    {/* Children */}
                                    <div className="popover-row">
                                        <div className="popover-label">
                                            <span>Children</span>
                                            <small>0 to 14 years</small>
                                        </div>
                                        <div className="popover-counter">
                                            <button 
                                                type="button" 
                                                disabled={passengers.child <= 0}
                                                onClick={() => updatePassenger('child', -1)}
                                            >−</button>
                                            <span>{passengers.child}</span>
                                            <button 
                                                type="button" 
                                                onClick={() => updatePassenger('child', 1)}
                                            >+</button>
                                        </div>
                                    </div>

                                    {/* Bikes */}
                                    <div className="popover-row">
                                        <div className="popover-label">
                                            <span>Bikes</span>
                                            <small>E-scooters not allowed</small>
                                        </div>
                                        <div className="popover-counter">
                                            <button 
                                                type="button" 
                                                disabled={passengers.bike <= 0}
                                                onClick={() => updatePassenger('bike', -1)}
                                            >−</button>
                                            <span>{passengers.bike}</span>
                                            <button 
                                                type="button" 
                                                onClick={() => updatePassenger('bike', 1)}
                                            >+</button>
                                        </div>
                                    </div>
                                </div>
                            )}
                        </div>

                        {/* Search Button */}
                        <button type="submit" className="main-search-btn" disabled={loading}>
                            {loading ? '...' : 'Search'}
                        </button>
                    </form>
                    
                    {error && <div className="search-error">{error}</div>}
                </div>
            </div>

            {/* Results Section (Same as before) */}
            {searched && (
                <div className="results-section">
                    <div className="results-container">
                        <h2>Available Routes for {new Date(date).toLocaleDateString()} ({getDayName(date)})</h2>
                        {loading ? (
                            <div className="loading-message">Finding the best routes for you...</div>
                        ) : searchResults.length > 0 ? (
                            <div className="results-list">
                                {searchResults.map(timetable => {
                                    const fromStop = timetable.timetableStops.find(s => s.station.id === parseInt(fromStationId));
                                    const toStop = timetable.timetableStops.find(s => s.station.id === parseInt(toStationId));
                                    
                                    if (!fromStop || !toStop) return null;

                                    const departureTime = new Date(fromStop.departureTime || fromStop.arrivalTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
                                    const arrivalTime = new Date(toStop.arrivalTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
                                    
                                    return (
                                        <div key={timetable.id} className="result-card">
                                            <div className="route-info">
                                                <div className="time-info">
                                                    <div className="departure">
                                                        <span className="time">{departureTime}</span>
                                                        <span className="station">{fromStop.station.name}</span>
                                                    </div>
                                                    <div className="duration-line">
                                                        <span className="duration">{formatDuration(timetable.route.durationMinutes)}</span>
                                                        <div className="line"></div>
                                                        <span className="days-info">
                                                            {timetable.daysOfWeek && timetable.daysOfWeek.length > 0 
                                                                ? timetable.daysOfWeek.join(', ') 
                                                                : 'Daily'}
                                                        </span>
                                                    </div>
                                                    <div className="arrival">
                                                        <span className="time">{arrivalTime}</span>
                                                        <span className="station">{toStop.station.name}</span>
                                                    </div>
                                                </div>
                                                <div className="meta-info">
                                                    <span className="vehicle-class">{timetable.route.vehicleClass}</span>
                                                    <span className="distance">{timetable.route.distance} km</span>
                                                </div>
                                            </div>
                                            <button 
                                                className="book-btn"
                                                onClick={() => handleBookClick(timetable)}
                                            >
                                                Book Now
                                            </button>
                                        </div>
                                    );
                                })}
                            </div>
                        ) : (
                            <div className="no-results">
                                <p>No routes found for the selected criteria.</p>
                                <p>Try changing the date or stations.</p>
                            </div>
                        )}
                    </div>
                </div>
            )}
        </div>
    );
}
