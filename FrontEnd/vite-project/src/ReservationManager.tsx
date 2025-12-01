import { useState, useEffect } from 'react';

// This is a React Function Component
function ReservationManager() {
    const [reservations, setReservations] = useState<any[]>([]);

    // Add the new enum fields to the form state with default values
    const [formData, setFormData] = useState({
        routeId: 1,
        passengerName: '',
        seatCount: 1,
        departureTime: '2025-11-20T10:00:00',
        arrivalTime: '2025-11-20T12:00:00',
        passengerCategory: 'ADULT', // Default value
        vehicleClass: 'SECOND_CLASS', // Default value
    });

    const API_URL = '/api/reservations'; // Use a relative path

    useEffect(() => {
        fetchReservations();
    }, []);

    const fetchReservations = async () => {
        try {
            const response = await fetch(API_URL);
            if (!response.ok) {
                console.error('Fetch error:', response.status);
                return;
            }
            const data = await response.json();
            setReservations(Array.isArray(data) ? data : []);
        } catch (error) {
            console.error('Backend is not responding. Is it running?', error);
        }
    };

    // Update the handler to work with select dropdowns as well
    const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
        const { name, value } = e.target;
        setFormData((prev) => ({
            ...prev,
            [name]: name === 'seatCount' || name === 'routeId' ? Number(value) : value,
        }));
    };

    const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();

        // The payload will now automatically include the new enum fields from the form state
        const payload = {
            ...formData,
            departureTime: new Date(formData.departureTime).toISOString(),
            arrivalTime: new Date(formData.arrivalTime).toISOString(),
        };

        try {
            const response = await fetch(API_URL, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload),
            });

            if (!response.ok) {
                const text = await response.text();
                console.error('POST error:', response.status, text);
                return;
            }

            await fetchReservations();
            setFormData((prev) => ({ ...prev, passengerName: '' }));
        } catch (error) {
            console.error('POST failed:', error);
        }
    };

    return (
        <div style={{ maxWidth: 600, margin: '2rem auto', fontFamily: 'sans-serif' }}>
            <h2>Reservation Manager</h2>

            <form onSubmit={handleSubmit} style={{ display: 'grid', gap: '0.75rem' }}>
                <label>
                    Route ID
                    <input type="number" name="routeId" value={formData.routeId} onChange={handleChange} min={1} />
                </label>

                <label>
                    Passenger Name
                    <input type="text" name="passengerName" value={formData.passengerName} onChange={handleChange} required />
                </label>

                <label>
                    Seat Count
                    <input type="number" name="seatCount" value={formData.seatCount} onChange={handleChange} min={1} />
                </label>

                {/* Add dropdown for Passenger Category */}
                <label>
                    Passenger Category
                    <select name="passengerCategory" value={formData.passengerCategory} onChange={handleChange}>
                        <option value="ADULT">Adult</option>
                        <option value="CHILD">Child</option>
                        <option value="STUDENT">Student</option>
                        <option value="PENSIONER">Pensioner</option>
                    </select>
                </label>

                {/* Add dropdown for Vehicle Class */}
                <label>
                    Vehicle Class
                    <select name="vehicleClass" value={formData.vehicleClass} onChange={handleChange}>
                        <option value="FIRST_CLASS">First Class</option>
                        <option value="SECOND_CLASS">Second Class</option>
                    </select>
                </label>

                <label>
                    Departure Time
                    <input type="datetime-local" name="departureTime" value={formData.departureTime} onChange={handleChange} />
                </label>

                <label>
                    Arrival Time
                    <input type="datetime-local" name="arrivalTime" value={formData.arrivalTime} onChange={handleChange} />
                </label>

                <button type="submit">Create Reservation</button>
            </form>

            <h3 style={{ marginTop: '2rem' }}>Existing reservations</h3>
            <ul>
                {reservations.map((r) => (
                    <li key={r.id ?? `${r.routeId}-${r.passengerName}-${r.departureTime}`}>
                        #{r.id ?? '—'} route {r.routeId} • {r.passengerName} ({r.passengerCategory}) • seats {r.seatCount} •
                        {` ${r.departureTime} → ${r.arrivalTime}`}
                    </li>
                ))}
                {reservations.length === 0 && <li>No reservations found.</li>}
            </ul>
        </div>
    );
}

export default ReservationManager;