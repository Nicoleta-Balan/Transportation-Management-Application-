import { useState, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { useStations } from '../../hooks/useStations';
import { routeApi } from '../../services/routeApi';
import { timetableApi } from '../../services/timetableApi';
import type { Route } from '../../types/Route';
import type { Timetable } from '../../types/Timetable';
import './SearchPage.css';

interface SearchFormValues {
    originId: string;
    destinationId: string;
    date: string;
}

interface TripResult {
    route: Route;
    timetable: Timetable;
    departureTime: string;
    arrivalTime: string;
    durationMinutes: number;
}

export default function SearchPage() {
    const { stations, loadStations } = useStations();
    const [results, setResults] = useState<TripResult[] | null>(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const { register, handleSubmit, watch } = useForm<SearchFormValues>({
        defaultValues: {
            date: new Date().toISOString().split('T')[0]
        }
    });

    const originId = watch('originId');

    useEffect(() => {
        loadStations();
    }, [loadStations]);

    const onSubmit = async (data: SearchFormValues) => {
        setLoading(true);
        setError(null);
        setResults(null);

        try {
            const originIdNum = parseInt(data.originId);
            const destinationIdNum = parseInt(data.destinationId);
            const searchDate = new Date(data.date);
            const dayOfWeek = searchDate.toLocaleDateString('en-US', { weekday: 'short' }).toUpperCase(); // Mon, Tue, etc.

            // 1. Find Routes
            const routes = await routeApi.searchRoutesByStations(originIdNum, destinationIdNum);

            if (routes.length === 0) {
                setResults([]);
                setLoading(false);
                return;
            }

            // 2. Find Timetables for each route
            const trips: TripResult[] = [];

            for (const route of routes) {
                const timetables = await timetableApi.getAllForRoute(route.id);

                for (const timetable of timetables) {
                    // Filter by date validity
                    if (timetable.startDate && new Date(timetable.startDate) > searchDate) continue;
                    if (timetable.endDate && new Date(timetable.endDate) < searchDate) continue;
                    
                    // Filter by day of week (if specified)
                    // Note: Backend might store days as "MONDAY", "TUESDAY" or "Mon", "Tue". 
                    // Assuming backend uses full names or 3-letter codes. 
                    // Let's assume the backend returns what we need or we handle it.
                    // If daysOfWeek is null/empty, assume daily? Or check logic.
                    // For now, let's assume if daysOfWeek is present, we check it.
                    if (timetable.daysOfWeek && timetable.daysOfWeek.length > 0) {
                        // Normalize to check. Assuming backend might return "MONDAY" or "Mon"
                        const isDayValid = timetable.daysOfWeek.some(d => 
                            d.toUpperCase().startsWith(dayOfWeek) || dayOfWeek.startsWith(d.toUpperCase())
                        );
                        if (!isDayValid) continue;
                    }

                    // Find stops
                    const originStop = timetable.timetableStops.find(s => s.station.id === originIdNum);
                    const destStop = timetable.timetableStops.find(s => s.station.id === destinationIdNum);

                    if (originStop && destStop && originStop.sequenceOrder < destStop.sequenceOrder) {
                        // Calculate duration
                        const depTime = new Date(originStop.departureTime || '');
                        const arrTime = new Date(destStop.arrivalTime);
                        
                        // Handle case where times are just time strings or full ISO strings
                        // If they are full ISO strings, we can diff them.
                        // If they are just times, we need to attach the date.
                        // The type definition says "ISO 8601 datetime string", so we assume full date.
                        
                        // However, timetables are usually recurring. 
                        // If the timetable stores "2023-01-01T10:00:00", that's a specific date.
                        // If it's a recurring schedule, it might store just time "10:00:00" or a dummy date.
                        // Let's assume for this MVP that the backend returns specific datetimes for the requested date 
                        // OR we just display the time part.
                        
                        // For display purposes:
                        const durationMs = new Date(destStop.arrivalTime).getTime() - new Date(originStop.departureTime || originStop.arrivalTime).getTime();
                        const durationMinutes = Math.round(durationMs / 60000);

                        trips.push({
                            route,
                            timetable,
                            departureTime: originStop.departureTime || originStop.arrivalTime,
                            arrivalTime: destStop.arrivalTime,
                            durationMinutes: durationMinutes > 0 ? durationMinutes : 0 // Fallback if calculation fails
                        });
                    }
                }
            }

            // Sort by departure time
            trips.sort((a, b) => new Date(a.departureTime).getTime() - new Date(b.departureTime).getTime());

            setResults(trips);
        } catch (err) {
            console.error(err);
            setError('Failed to search for routes. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    const formatTime = (isoString: string) => {
        try {
            return new Date(isoString).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
        } catch (e) {
            return isoString;
        }
    };

    const formatDuration = (minutes: number) => {
        const h = Math.floor(minutes / 60);
        const m = minutes % 60;
        return `${h}h ${m}m`;
    };

    return (
        <div className="search-page">
            <section className="search-hero">
                <div className="user-container">
                    <h1 className="hero-title">Travel with comfort</h1>
                    <p className="hero-subtitle">Find the best routes for your next journey</p>
                    
                    <form className="search-container" onSubmit={handleSubmit(onSubmit)}>
                        <div className="form-group">
                            <label className="form-label">From</label>
                            <select className="form-select" {...register('originId', { required: true })}>
                                <option value="">Select Origin</option>
                                {stations.map(station => (
                                    <option key={station.id} value={station.id}>{station.name}</option>
                                ))}
                            </select>
                        </div>
                        
                        <div className="form-group">
                            <label className="form-label">To</label>
                            <select className="form-select" {...register('destinationId', { required: true })}>
                                <option value="">Select Destination</option>
                                {stations.map(station => (
                                    // Prevent selecting same station
                                    station.id !== parseInt(originId) && (
                                        <option key={station.id} value={station.id}>{station.name}</option>
                                    )
                                ))}
                            </select>
                        </div>
                        
                        <div className="form-group">
                            <label className="form-label">Date</label>
                            <input 
                                type="date" 
                                className="form-input" 
                                {...register('date', { required: true })}
                                min={new Date().toISOString().split('T')[0]}
                            />
                        </div>
                        
                        <button type="submit" className="search-button" disabled={loading}>
                            {loading ? 'Searching...' : 'Search'}
                        </button>
                    </form>
                </div>
            </section>

            {error && (
                <div className="user-container" style={{ marginTop: '2rem' }}>
                    <div className="error-alert">{error}</div>
                </div>
            )}

            {results && (
                <section className="results-section">
                    <div className="results-container">
                        <h2 className="results-title">
                            {results.length > 0 
                                ? `Found ${results.length} results` 
                                : 'No routes found for your search'}
                        </h2>
                        
                        {results.length === 0 && (
                            <div className="no-results">
                                <p>Try selecting different stations or dates.</p>
                            </div>
                        )}

                        {results.map((trip, index) => (
                            <div key={`${trip.timetable.id}-${index}`} className="route-card">
                                <div className="route-info">
                                    <div className="time-info">
                                        <div className="time">{formatTime(trip.departureTime)}</div>
                                        <div className="station">{trip.route.originStation?.name}</div>
                                    </div>
                                    
                                    <div className="duration-info">
                                        <span>{formatDuration(trip.durationMinutes)}</span>
                                        <div className="duration-line"></div>
                                        <span>Direct</span>
                                    </div>
                                    
                                    <div className="time-info">
                                        <div className="time">{formatTime(trip.arrivalTime)}</div>
                                        <div className="station">{trip.route.destinationStation?.name}</div>
                                    </div>
                                </div>
                                
                                <div className="price-info">
                                    <span className="price">$25.00</span>
                                    <button className="book-button">Book Seat</button>
                                </div>
                            </div>
                        ))}
                    </div>
                </section>
            )}
        </div>
    );
}
