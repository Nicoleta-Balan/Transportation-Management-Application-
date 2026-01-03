import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useStations } from '../../hooks/useStations';
import { timetableApi } from '../../services/timetableApi';
import type { Timetable } from '../../types/Timetable';
import './SearchPage.css';

export default function SearchPage() {
    const navigate = useNavigate();
    const { stations } = useStations();

    const [fromStationId, setFromStationId] = useState<string>('');
    const [toStationId, setToStationId] = useState<string>('');
    const [date, setDate] = useState('');
    const [passengers, setPassengers] = useState(1);

    const [searchResults, setSearchResults] = useState<Timetable[]>([]);
    const [searched, setSearched] = useState(false);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    // Set default date to today
    useEffect(() => {
        const today = new Date().toISOString().split('T')[0];
        setDate(today);
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
            console.log('Searching routes...');
            
            // Use the API service instead of direct fetch
            const results = await timetableApi.searchTimetables(
                parseInt(fromStationId),
                parseInt(toStationId),
                date
            );
            
            console.log('Search results:', results);
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

    // Filter available stations for "To" dropdown
    const availableToStations = stations.filter(s => s.id.toString() !== fromStationId);

    // Format duration
    const formatDuration = (minutes: number) => {
        if (!minutes) return '0h 0m';
        const hrs = Math.floor(minutes / 60);
        const mins = minutes % 60;
        return `${hrs}h ${mins}m`;
    };

    // Helper to get day name from date string
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
                <div className="search-container">
                    <h1>Plan Your Journey</h1>
                    <form onSubmit={handleSearch} className="search-form">
                        <div className="form-group">
                            <label>From</label>
                            <select
                                value={fromStationId}
                                onChange={(e) => {
                                    setFromStationId(e.target.value);
                                    if (e.target.value === toStationId) {
                                        setToStationId('');
                                    }
                                }}
                                required
                            >
                                <option value="">Select Origin</option>
                                {stations.map(station => (
                                    <option key={station.id} value={station.id}>
                                        {station.name}
                                    </option>
                                ))}
                            </select>
                        </div>
                        <div className="form-group">
                            <label>To</label>
                            <select
                                value={toStationId}
                                onChange={(e) => setToStationId(e.target.value)}
                                required
                                disabled={!fromStationId}
                            >
                                <option value="">Select Destination</option>
                                {availableToStations.map(station => (
                                    <option key={station.id} value={station.id}>
                                        {station.name}
                                    </option>
                                ))}
                            </select>
                        </div>
                        <div className="form-group">
                            <label>Departure</label>
                            <input
                                type="date"
                                value={date}
                                min={new Date().toISOString().split('T')[0]}
                                onChange={(e) => setDate(e.target.value)}
                                required
                            />
                        </div>
                        <div className="form-group">
                            <label>Passengers</label>
                            <input
                                type="number"
                                min="1"
                                max="10"
                                value={passengers}
                                onChange={(e) => setPassengers(parseInt(e.target.value))}
                                required
                            />
                        </div>
                        <button type="submit" className="search-btn" disabled={loading}>
                            {loading ? 'Searching...' : 'Search'}
                        </button>
                    </form>

                    {error && <div className="search-error">{error}</div>}
                </div>
            </div>

            {searched && (
                <div className="results-section">
                    <div className="results-container">
                        <h2>Available Routes for {new Date(date).toLocaleDateString()} ({getDayName(date)})</h2>
                        {loading ? (
                            <div className="loading-message">Finding the best routes for you...</div>
                        ) : searchResults.length > 0 ? (
                            <div className="results-list">
                                {searchResults.map(timetable => {
                                    // Find the specific stops for origin and destination to show times
                                    // We use timetable stops here because they contain the specific times for this schedule
                                    const fromStop = timetable.timetableStops.find(s => s.station.id === parseInt(fromStationId));
                                    const toStop = timetable.timetableStops.find(s => s.station.id === parseInt(toStationId));
                                    
                                    // If timetable stops are missing (e.g. only start/end defined in timetable but route has intermediates),
                                    // we might need to fallback or handle it. For now, we assume if the search returned this timetable,
                                    // it's valid, but we need to display something.
                                    
                                    if (!fromStop || !toStop) {
                                        // Fallback: Use the first and last stops if specific ones aren't found in timetable stops
                                        // This handles cases where intermediate stops might not have specific times in the timetable yet
                                        // but the route connects them.
                                        // However, for a valid schedule, they should be there.
                                        return null; 
                                    }

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
                                            <button className="book-btn">Book Now</button>
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
