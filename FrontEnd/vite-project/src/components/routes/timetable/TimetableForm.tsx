import { useState, useCallback, useMemo, useEffect, useRef } from 'react';
import type { Route } from '../../../types/Route';
import type { Timetable, CreateTimetableRequest, UpdateTimetableRequest, TimetableStopRequest } from '../../../types/Timetable';
import { timetableApi } from '../../../services/timetableApi';
import { getErrorMessage } from '../../../utils/errorUtils';
import { SubmitButton, CancelButton } from '../../buttons';
import { ErrorAlert } from '../../ErrorAlert';
import { FormTextarea } from '../../forms/FormTextarea';
import { MAX_ROUTE_DESCRIPTION_LENGTH } from '../../../constants/calculationConstants';
import { extractTime, extractDate } from '../../../utils/dateTimeUtils';
import './TimetableForm.css';

interface TimetableFormProps {
    route: Route;
    timetable?: Timetable | null;
    onSave: () => Promise<void>;
    onCancel?: () => void;
}

export function TimetableForm({ route, timetable, onSave, onCancel }: TimetableFormProps) {
    const isEditing = !!timetable;
    
    // Extract date from timetable stops if editing
    const extractDateFromTimetable = useCallback((t: Timetable): string => {
        if (t.timetableStops && t.timetableStops.length > 0) {
            const firstStop = t.timetableStops[0];
            const timeStr = firstStop.departureTime || firstStop.arrivalTime;
            if (timeStr) {
                return extractDate(timeStr);
            }
        }
        return '';
    }, []);

    // Initialize form state from timetable if editing
    const [selectedDays, setSelectedDays] = useState<string[]>(() => {
        if (timetable?.daysOfWeek) {
            return [...timetable.daysOfWeek];
        }
        return [];
    });
    const [startDate, setStartDate] = useState(() => {
        if (timetable?.startDate) {
            return timetable.startDate;
        }
        return timetable ? extractDateFromTimetable(timetable) : '';
    });
    const [endDate, setEndDate] = useState(() => {
        if (timetable?.endDate) {
            return timetable.endDate;
        }
        if (timetable && timetable.timetableStops && timetable.timetableStops.length > 0) {
            const lastStop = timetable.timetableStops[timetable.timetableStops.length - 1];
            const timeStr = lastStop.arrivalTime;
            if (timeStr) {
                const date = new Date(timeStr);
                return date.toISOString().split('T')[0];
            }
        }
        return '';
    });
    const [description, setDescription] = useState(() => timetable?.description || '');
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState<string | null>(null);

    // Per-station times and stop minutes
    const routeStops = useMemo(() => {
        if (!route.routeStops || route.routeStops.length === 0) {
            return [];
        }
        return [...route.routeStops].sort((a, b) => a.sequenceOrder - b.sequenceOrder);
    }, [route.routeStops]);

    // Initialize times from timetable if editing
    const initializeTimesFromTimetable = useCallback(() => {
        if (!timetable || !timetable.timetableStops || routeStops.length === 0) {
            return {
                firstDeparture: '',
                arrivals: Array(routeStops.length).fill(''),
                departures: Array(routeStops.length).fill(''),
                stops: Array(routeStops.length).fill(1),
            };
        }

        const firstStop = timetable.timetableStops[0];
        const firstDeparture = firstStop.departureTime 
            ? extractTime(firstStop.departureTime)
            : firstStop.arrivalTime 
                ? extractTime(firstStop.arrivalTime)
                : '';

        const arrivals: string[] = [];
        const departures: string[] = [];
        const stops: number[] = [];

        timetable.timetableStops.forEach((stop, index) => {
            if (index === 0) {
                arrivals[index] = '';
                departures[index] = firstDeparture;
                stops[index] = 1; // Not used for first station
            } else {
                arrivals[index] = stop.arrivalTime ? extractTime(stop.arrivalTime) : '';
                
                if (stop.departureTime) {
                    const depTime = extractTime(stop.departureTime);
                    departures[index] = depTime;
                    
                    // Calculate stop minutes (difference between arrival and departure)
                    if (arrivals[index]) {
                        const arrDate = new Date(`2000-01-01T${arrivals[index]}`);
                        const depDate = new Date(`2000-01-01T${depTime}`);
                        const diffMinutes = (depDate.getTime() - arrDate.getTime()) / (1000 * 60);
                        stops[index] = Math.max(1, Math.min(999, diffMinutes));
                    } else {
                        stops[index] = 1;
                    }
                } else {
                    departures[index] = '';
                    stops[index] = 1;
                }
            }
        });

        // Fill remaining slots if route has more stops than timetable
        while (arrivals.length < routeStops.length) {
            arrivals.push('');
            departures.push('');
            stops.push(1);
        }

        return { firstDeparture, arrivals, departures, stops };
        }, [timetable, routeStops.length]);

    const initialTimes = useMemo(() => initializeTimesFromTimetable(), [initializeTimesFromTimetable]);

    const [firstStationDeparture, setFirstStationDeparture] = useState<string>(initialTimes.firstDeparture);
    const [arrivalTimes, setArrivalTimes] = useState<string[]>(initialTimes.arrivals);
    const [departureTimes, setDepartureTimes] = useState<string[]>(initialTimes.departures);
    const [stopMinutes, setStopMinutes] = useState<number[]>(initialTimes.stops);

    // Update times when timetable changes
    useEffect(() => {
        if (timetable) {
            const times = initializeTimesFromTimetable();
            setFirstStationDeparture(times.firstDeparture);
            setArrivalTimes(times.arrivals);
            setDepartureTimes(times.departures);
            setStopMinutes(times.stops);
            setDescription(timetable.description || '');
            setStartDate(timetable.startDate || extractDateFromTimetable(timetable));
            if (timetable.endDate) {
                setEndDate(timetable.endDate);
            } else if (timetable.timetableStops && timetable.timetableStops.length > 0) {
                const lastStop = timetable.timetableStops[timetable.timetableStops.length - 1];
                if (lastStop.arrivalTime) {
                    const date = new Date(lastStop.arrivalTime);
                    setEndDate(date.toISOString().split('T')[0]);
                }
            }
            if (timetable.daysOfWeek) {
                setSelectedDays([...timetable.daysOfWeek]);
            }
        }
    }, [timetable, initializeTimesFromTimetable, extractDateFromTimetable]);

    const dayOptions = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];

    const toggleDay = (day: string) => {
        setSelectedDays((prev) =>
            prev.includes(day) ? prev.filter((d) => d !== day) : [...prev, day]
        );
    };

    // Helper to add minutes to a time string (HH:MM format)
    const addMinutesToTime = (time: string, minutes: number): string => {
        if (!time) return '';
        const [h, m] = time.split(':').map(Number);
        const totalMinutes = (h * 60) + m + minutes;
        const newHours = Math.floor(totalMinutes / 60) % 24;
        const newMinutes = totalMinutes % 60;
        return `${String(newHours).padStart(2, '0')}:${String(newMinutes).padStart(2, '0')}`;
    };

    // Track if we're currently calculating to prevent infinite loops
    const isCalculatingRef = useRef(false);

    // Calculate times for all stations based on first departure and durations
    const calculateTimes = useCallback(() => {
        if (!firstStationDeparture || routeStops.length === 0 || isCalculatingRef.current) {
            return;
        }

        isCalculatingRef.current = true;

        const newArrivalTimes: string[] = [];
        const newDepartureTimes: string[] = [];

        // First station: departure only
        newDepartureTimes[0] = firstStationDeparture;

        // Calculate subsequent stations
        for (let i = 1; i < routeStops.length; i++) {
            const isEnd = i === routeStops.length - 1;
            const prevDeparture = newDepartureTimes[i - 1];
            if (!prevDeparture) break;

            const durationMinutes = routeStops[i].durationMinutesFromPrevious || 0;
            const arrival = addMinutesToTime(prevDeparture, durationMinutes);
            newArrivalTimes[i] = arrival;

            // Last stop (end destination) doesn't have departure
            if (!isEnd) {
                // Departure = arrival + stop minutes
                const stop = stopMinutes[i] ?? 1;
                newDepartureTimes[i] = addMinutesToTime(arrival, stop);
            }
        }

        setArrivalTimes((prev) => {
            const updated = [...prev];
            newArrivalTimes.forEach((time, idx) => {
                if (time) updated[idx] = time;
            });
            return updated;
        });

        setDepartureTimes((prev) => {
            const updated = [...prev];
            newDepartureTimes.forEach((time, idx) => {
                if (time) updated[idx] = time;
            });
            return updated;
        });

        isCalculatingRef.current = false;
    }, [firstStationDeparture, routeStops, stopMinutes]);

    // Recalculate times when first station departure changes
    useEffect(() => {
        if (firstStationDeparture) {
            calculateTimes();
        }
    }, [firstStationDeparture, calculateTimes]);

    const handleFirstStationDepartureChange = (value: string) => {
        setFirstStationDeparture(value);
    };

    const handleArrivalChange = (index: number, value: string) => {
        const isEnd = index === routeStops.length - 1;
        
        setArrivalTimes((prev) => {
            const next = [...prev];
            next[index] = value;
            return next;
        });

        // Last stop (end destination) doesn't have departure
        if (!isEnd) {
            // Recalculate departure for this station (arrival + stop)
            const stop = stopMinutes[index] ?? 1;
            const newDeparture = addMinutesToTime(value, stop);
            setDepartureTimes((prev) => {
                const next = [...prev];
                next[index] = newDeparture;
                return next;
            });

            // Recalculate subsequent stations
            if (index < routeStops.length - 1) {
                const newArrivalTimes: string[] = [];
                const newDepartureTimes: string[] = [];
                
                for (let i = index + 1; i < routeStops.length; i++) {
                    const isEndStation = i === routeStops.length - 1;
                    const prevDeparture = i === index + 1 ? newDeparture : newDepartureTimes[i - 1];
                    if (!prevDeparture) break;

                    const durationMinutes = routeStops[i].durationMinutesFromPrevious || 0;
                    const arrival = addMinutesToTime(prevDeparture, durationMinutes);
                    newArrivalTimes[i] = arrival;

                    // Last stop (end destination) doesn't have departure
                    if (!isEndStation) {
                        const stop = stopMinutes[i] ?? 1;
                        newDepartureTimes[i] = addMinutesToTime(arrival, stop);
                    }
                }

                setArrivalTimes((prev) => {
                    const next = [...prev];
                    newArrivalTimes.forEach((time, idx) => {
                        if (time) next[idx] = time;
                    });
                    return next;
                });

                setDepartureTimes((prev) => {
                    const next = [...prev];
                    newDepartureTimes.forEach((time, idx) => {
                        if (time) next[idx] = time;
                    });
                    return next;
                });
            }
        }
    };

    const handleDepartureChange = (index: number, value: string) => {
        if (isCalculatingRef.current) return;
        
        // Prevent setting departure time for end station
        const isEnd = index === routeStops.length - 1;
        if (isEnd) {
            setError(`${routeStops[index].station.name} is the end station and should not have a stop time or a departure time.`);
            return;
        }

        setDepartureTimes((prev) => {
            const next = [...prev];
            next[index] = value;
            return next;
        });

        // Recalculate subsequent stations
        if (index < routeStops.length - 1) {
            isCalculatingRef.current = true;
            const newArrivalTimes: string[] = [];
            const newDepartureTimes: string[] = [];
            
            for (let i = index + 1; i < routeStops.length; i++) {
                const isEnd = i === routeStops.length - 1;
                const prevDeparture = i === index + 1 ? value : newDepartureTimes[i - 1];
                if (!prevDeparture) break;

                const durationMinutes = routeStops[i].durationMinutesFromPrevious || 0;
                const arrival = addMinutesToTime(prevDeparture, durationMinutes);
                newArrivalTimes[i] = arrival;

                // Last stop (end destination) doesn't have departure
                if (!isEnd) {
                    const stop = stopMinutes[i] ?? 1;
                    newDepartureTimes[i] = addMinutesToTime(arrival, stop);
                }
            }

            setArrivalTimes((prev) => {
                const next = [...prev];
                newArrivalTimes.forEach((time, idx) => {
                    if (time) next[idx] = time;
                });
                return next;
            });

            setDepartureTimes((prev) => {
                const next = [...prev];
                newDepartureTimes.forEach((time, idx) => {
                    if (time) next[idx] = time;
                });
                return next;
            });
            isCalculatingRef.current = false;
        }
    };

    const handleStopChange = (index: number, value: number) => {
        if (isCalculatingRef.current) return;
        
        // Prevent setting stop time for end station
        const isEnd = index === routeStops.length - 1;
        if (isEnd) {
            setError(`${routeStops[index].station.name} is the end station and should not have a stop time or a departure time.`);
            return;
        }

        const clamped = Math.min(999, Math.max(1, value || 1));
        setStopMinutes((prev) => {
            const next = [...prev];
            next[index] = clamped;
            return next;
        });

        // Recalculate departure for this station (arrival + stop)
        const arrival = arrivalTimes[index];
        
        if (arrival && !isEnd) {
            const newDeparture = addMinutesToTime(arrival, clamped);
            setDepartureTimes((prev) => {
                const next = [...prev];
                next[index] = newDeparture;
                return next;
            });

            // Recalculate subsequent stations
            if (index < routeStops.length - 1) {
                isCalculatingRef.current = true;
                const newArrivalTimes: string[] = [];
                const newDepartureTimes: string[] = [];
                
                for (let i = index + 1; i < routeStops.length; i++) {
                    const isEndStation = i === routeStops.length - 1;
                    const prevDeparture = i === index + 1 ? newDeparture : newDepartureTimes[i - 1];
                    if (!prevDeparture) break;

                    const durationMinutes = routeStops[i].durationMinutesFromPrevious || 0;
                    const arrival = addMinutesToTime(prevDeparture, durationMinutes);
                    newArrivalTimes[i] = arrival;

                    // Last stop (end destination) doesn't have departure
                    if (!isEndStation) {
                        const stop = stopMinutes[i] ?? 1;
                        newDepartureTimes[i] = addMinutesToTime(arrival, stop);
                    }
                }

                setArrivalTimes((prev) => {
                    const next = [...prev];
                    newArrivalTimes.forEach((time, idx) => {
                        if (time) next[idx] = time;
                    });
                    return next;
                });

                setDepartureTimes((prev) => {
                    const next = [...prev];
                    newDepartureTimes.forEach((time, idx) => {
                        if (time) next[idx] = time;
                    });
                    return next;
                });
                isCalculatingRef.current = false;
            }
        }
    };

    const handleSubmit = useCallback(async (e: React.FormEvent) => {
        e.preventDefault();

        if (selectedDays.length === 0) {
            setError('Please select at least one day of the week.');
            return;
        }

        if (!startDate) {
            setError('Please select a start date.');
            return;
        }

        if (!endDate) {
            setError('Please select an end date.');
            return;
        }

        if (!firstStationDeparture) {
            setError('Please set departure time for the first station.');
            return;
        }

        if (routeStops.length === 0) {
            setError('Route must have at least one stop');
            return;
        }

        if (!description.trim()) {
            setError('Please enter a description.');
            return;
        }

        // Validate all stations have required times
        for (let i = 1; i < routeStops.length; i++) {
            const isEnd = i === routeStops.length - 1;
            
            if (!arrivalTimes[i]) {
                setError(`Please set arrival time for ${routeStops[i].station.name}.`);
                return;
            }
            
            // End station (last stop) doesn't need departure time
            if (!isEnd && !departureTimes[i]) {
                setError(`Please set departure time for ${routeStops[i].station.name}.`);
                return;
            }
        }

        setSubmitting(true);
        setError(null);

        try {
            const baseDate = startDate;
            const combineDateAndTime = (date: string, time: string): string => {
                // Create ISO string in local timezone by manually formatting
                // Format: YYYY-MM-DDTHH:mm:ss (local time, no timezone offset)
                const [h, m] = time.split(':').map(Number);
                const hours = String(h ?? 0).padStart(2, '0');
                const minutes = String(m ?? 0).padStart(2, '0');
                return `${date}T${hours}:${minutes}:00`;
            };

            const stops: TimetableStopRequest[] = routeStops.map((stop, index) => {
                const isEnd = index === routeStops.length - 1;
                
                // First station: no arrival, departure from firstStationDeparture
                if (index === 0) {
                    const departureDateTime = combineDateAndTime(baseDate, firstStationDeparture);
                    return {
                        stationId: stop.station.id,
                        sequenceOrder: index,
                        arrivalTime: departureDateTime, // Same as departure for start
                        departureTime: departureDateTime,
                    };
                }

                // Last station (end destination): only arrival, no departure
                if (isEnd) {
                    const arrivalTimeStr = arrivalTimes[index] || '';
                    const arrivalDateTime = arrivalTimeStr
                        ? combineDateAndTime(baseDate, arrivalTimeStr)
                        : `${baseDate}T00:00:00`;
                    
                    return {
                        stationId: stop.station.id,
                        sequenceOrder: index,
                        arrivalTime: arrivalDateTime,
                        departureTime: null, // No departure for end destination
                    };
                }

                // Intermediate stations: arrival and departure
                const arrivalTimeStr = arrivalTimes[index] || '';
                const departureTimeStr = departureTimes[index] || '';

                const arrivalDateTime = arrivalTimeStr
                    ? combineDateAndTime(baseDate, arrivalTimeStr)
                    : `${baseDate}T00:00:00`;
                const departureDateTime = departureTimeStr
                    ? combineDateAndTime(baseDate, departureTimeStr)
                    : null;

                return {
                    stationId: stop.station.id,
                    sequenceOrder: index,
                    arrivalTime: arrivalDateTime,
                    departureTime: departureDateTime,
                };
            });

            if (isEditing && timetable) {
                const updateRequest: UpdateTimetableRequest = {
                    description: description.trim() || undefined,
                    startDate: startDate || undefined,
                    endDate: endDate || undefined,
                    daysOfWeek: selectedDays.length > 0 ? selectedDays : undefined,
                    stops,
                };
                await timetableApi.updateTimetable(timetable.id, updateRequest);
            } else {
                const request: CreateTimetableRequest = {
                    routeId: route.id,
                    description: description.trim() || undefined,
                    startDate: startDate || undefined,
                    endDate: endDate || undefined,
                    daysOfWeek: selectedDays.length > 0 ? selectedDays : undefined,
                    stops,
                };
                await timetableApi.createTimetable(request);
            }
            
            await onSave();
            
            // Reset form only if creating (not editing)
            if (!isEditing) {
                setStartDate('');
                setEndDate('');
                setDescription('');
                setSelectedDays([]);
                setFirstStationDeparture('');
                setArrivalTimes(Array(routeStops.length).fill(''));
                setDepartureTimes(Array(routeStops.length).fill(''));
                setStopMinutes(Array(routeStops.length).fill(1));
            }
        } catch (err) {
            const errorMessage = getErrorMessage(err, isEditing ? 'Failed to update timetable' : 'Failed to create timetable');
            setError(errorMessage);
            console.error(isEditing ? 'Timetable update error:' : 'Timetable creation error:', err);
        } finally {
            setSubmitting(false);
        }
    }, [selectedDays, startDate, endDate, description, routeStops, firstStationDeparture, arrivalTimes, departureTimes, stopMinutes, route.id, onSave, isEditing, timetable]);

    if (routeStops.length === 0) {
        return <div className="error-text">Route must have at least one stop to create a timetable.</div>;
    }

    return (
        <form onSubmit={handleSubmit} className="timetable-form" noValidate>
            <div className="timetable-top">
                <div className="day-selector">
                    <span className="day-label">Days:</span>
                    <div className="day-options">
                        {dayOptions.map((day) => (
                            <label key={day} className="day-option">
                                <input
                                    type="checkbox"
                                    checked={selectedDays.includes(day)}
                                    onChange={() => toggleDay(day)}
                                />
                                {day}
                            </label>
                        ))}
                    </div>
                </div>

                <div className="timetable-top-grid">
                    <div className="form-group inline-input">
                        <label htmlFor="timetable-start">Start Date</label>
                        <input
                            id="timetable-start"
                            type="date"
                            value={startDate}
                            onChange={(e) => setStartDate(e.target.value)}
                            required
                            className="time-input"
                        />
                    </div>

                    <div className="form-group inline-input">
                        <label htmlFor="timetable-end">End Date</label>
                        <input
                            id="timetable-end"
                            type="date"
                            value={endDate}
                            onChange={(e) => setEndDate(e.target.value)}
                            required
                            className="time-input"
                        />
                    </div>

                    <div className="form-group description-inline">
                        <FormTextarea
                            id="timetable-description"
                            label="Description"
                            placeholder={`Optional (max ${MAX_ROUTE_DESCRIPTION_LENGTH} chars)`}
                            rows={2}
                            maxLength={255}
                            value={description}
                            onChange={(e) => setDescription(e.target.value)}
                        />
                    </div>
                </div>
            </div>

            <div className="timetable-stations-list">
                <table className="timetable-stations-table">
                    <thead>
                        <tr>
                            <th>Station</th>
                            <th>Distance from Previous</th>
                            <th>Duration from Previous</th>
                            <th>Arrival Time</th>
                            <th>Stop (min)</th>
                            <th>Departure Time</th>
                        </tr>
                    </thead>
                    <tbody>
                        {routeStops.map((stop, index) => {
                            const isStart = index === 0;
                            const isEnd = index === routeStops.length - 1;
                            const arrivalValue = arrivalTimes[index] || '';
                            const departureValue = isStart 
                                ? firstStationDeparture 
                                : departureTimes[index] || '';
                            const stopValue = stopMinutes[index] ?? 1;

                            return (
                                <tr key={stop.station.id}>
                                    <td>
                                        <strong>{stop.station.name}</strong>
                                        {stop.station.address && (
                                            <div className="station-address">{stop.station.address}</div>
                                        )}
                                    </td>
                                    <td>
                                        {index > 0 
                                            ? `${stop.distanceFromPrevious.toFixed(1)} km`
                                            : '-'
                                        }
                                    </td>
                                    <td>
                                        {index > 0
                                            ? `${stop.durationMinutesFromPrevious} min`
                                            : '-'
                                        }
                                    </td>
                                    <td>
                                        {isStart ? (
                                            <span className="no-arrival">-</span>
                                        ) : (
                                            <div>
                                                <input
                                                    type="time"
                                                    value={arrivalValue}
                                                    onChange={(e) => handleArrivalChange(index, e.target.value)}
                                                    required
                                                    className="time-input"
                                                />
                                            </div>
                                        )}
                                    </td>
                                    <td>
                                        {isStart || isEnd ? (
                                            <span className="no-stop">-</span>
                                        ) : (
                                            <input
                                                type="number"
                                                min={1}
                                                max={999}
                                                value={stopValue}
                                                onChange={(e) => handleStopChange(index, parseInt(e.target.value, 10))}
                                                className="stop-input"
                                            />
                                        )}
                                    </td>
                                    <td>
                                        {isEnd ? (
                                            <span className="no-departure">-</span>
                                        ) : (
                                            <input
                                                type="time"
                                                value={departureValue}
                                                onChange={(e) => isStart 
                                                    ? handleFirstStationDepartureChange(e.target.value)
                                                    : handleDepartureChange(index, e.target.value)
                                                }
                                                required
                                                className="time-input"
                                            />
                                        )}
                                    </td>
                                </tr>
                            );
                        })}
                    </tbody>
                </table>
            </div>

            <ErrorAlert error={error} />

            <div className="form-actions">
                <SubmitButton
                    mode="save"
                    isSubmitting={submitting}
                    isValid={Boolean(
                        selectedDays.length > 0 &&
                        startDate &&
                        endDate &&
                        description.trim() &&
                        firstStationDeparture &&
                        routeStops.length > 0 &&
                        routeStops.every((_, idx) => {
                            if (idx === 0) return true; // First station only needs departure
                            if (idx === routeStops.length - 1) return arrivalTimes[idx]; // Last station only needs arrival
                            return arrivalTimes[idx] && departureTimes[idx]; // Intermediate stations need both
                        })
                    )}
                    submittingLabel="Saving..."
                    ariaLabel="Save timetable"
                />
                {onCancel && (
                    <CancelButton 
                        onClick={onCancel} 
                        disabled={submitting}
                        ariaLabel="Cancel editing timetable"
                    />
                )}
            </div>
        </form>
    );
}

