import { useState, useEffect } from 'react';

// This is a React Function Component
function ReservationManager() {
    // This state holds the list of reservations we get from the server.
    // Example: [ { id: 1, passengerName: "Alice", ... }, { id: 2, ... } ]
    const [reservations, setReservations] = useState<any[]>([]);

    // This state holds the values from the form you can edit on the page.
    // These are the fields we plan to send to the backend when you click "Create Reservation".
    const [formData, setFormData] = useState({
        routeId: 1,
        // Passenger name typed by the user.
        passengerName: '',
        // How many seats to reserve (must be a number).
        seatCount: 1,
        // These are ISO date-time values. You can change them in the form as well.
        departureTime: '2025-11-20T10:00:00',
        arrivalTime: '2025-11-20T12:00:00',
    });

    const API_URL = 'http://localhost:8085/api/reservations';

    // useEffect runs after the component first appears on the screen.
    // We use it here to load the current reservations list from the server.
    useEffect(() => {
        fetchReservations();
    }, []); // [] means "run only once" when the component first mounts.

    // This function talks to the backend (GET) to read the list of reservations.
    const fetchReservations = async () => {
        try {
            // Ask the server for reservations
            const response = await fetch(API_URL);

            // If the server did not respond with a good status (like 200 OK)
            if (!response.ok) {
                console.error('Fetch error:', response.status);
                return; // Stop here if there was an error status
            }

            // Convert the response (which is text) into a JavaScript array/object
            const data = await response.json();

            // Make sure it's an array before we set it
            setReservations(Array.isArray(data) ? data : []);
        } catch (error) {
            // This runs if the network request failed
            console.error('Backend is not responding. Is it running?', error);
        }
    };

    // When you type in the form inputs, this function updates our formData state.
    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;

        // For number fields, we convert the string input to a number so the backend receives numbers, not strings.
        setFormData((prev) => ({
            ...prev,
            [name]: name === 'seatCount' || name === 'routeId' ? Number(value) : value,
        }));
    };

    // This function runs when you click the "Create Reservation" button
    const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault(); // Prevent the browser from reloading the page.

        // Prepare the data to be sent, ensuring dates are in a format Spring Boot understands
        const payload = {
            ...formData,
            departureTime: new Date(formData.departureTime).toISOString(),
            arrivalTime: new Date(formData.arrivalTime).toISOString(),
        };

        try {
            // Send the formData to the backend as JSON with a POST request.
            const response = await fetch(API_URL, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload), // Convert JS object to JSON string
            });

            // If the server returns an error (like 400/500), log it so we can see what happened.
            if (!response.ok) {
                const text = await response.text();
                console.error('POST error:', response.status, text);
                return; // Stop if there was an error
            }

            // If all went well, refresh the reservations list so we see the new item.
            await fetchReservations();

            // Optional: Clear just the passenger name field to be ready for the next entry.
            setFormData((prev) => ({ ...prev, passengerName: '' }));
        } catch (error) {
            // If the network failed, this will run.
            console.error('POST failed:', error);
        }
    };

    // This is what gets shown on the screen: a form and the list of reservations.
    return (
        <div style={{ maxWidth: 600, margin: '2rem auto', fontFamily: 'sans-serif' }}>
            <h2>Reservation Manager</h2>

            {/* The form for creating a reservation */}
            <form onSubmit={handleSubmit} style={{ display: 'grid', gap: '0.75rem' }}>
                <label>
                    Route ID
                    <input
                        type="number"
                        name="routeId"
                        value={formData.routeId}
                        onChange={handleChange}
                        min={1}
                        // Tip: if you don’t know the route ID, check your backend/database
                    />
                </label>

                <label>
                    Passenger Name
                    <input
                        type="text"
                        name="passengerName"
                        value={formData.passengerName}
                        onChange={handleChange}
                        required
                        // required means the browser will force this not to be empty
                    />
                </label>

                <label>
                    Seat Count
                    <input
                        type="number"
                        name="seatCount"
                        value={formData.seatCount}
                        onChange={handleChange}
                        min={1}
                    />
                </label>

                {/* "datetime-local" shows a date and time picker in most browsers */}
                <label>
                    Departure Time
                    <input
                        type="datetime-local"
                        name="departureTime"
                        value={formData.departureTime}
                        onChange={handleChange}
                    />
                </label>

                <label>
                    Arrival Time
                    <input
                        type="datetime-local"
                        name="arrivalTime"
                        value={formData.arrivalTime}
                        onChange={handleChange}
                    />
                </label>

                <button type="submit">Create Reservation</button>
            </form>

            {/* The list of existing reservations */}
            <h3 style={{ marginTop: '2rem' }}>Existing reservations</h3>
            <ul>
                {reservations.map((r) => (
                    // We need a stable key. If the backend sends `id`, we use it.
                    <li key={r.id ?? `${r.routeId}-${r.passengerName}-${r.departureTime}`}>
                        {/* We show a few useful fields. Adjust names if your backend differs. */}
                        #{r.id ?? '—'} route {r.routeId} • {r.passengerName} • seats {r.seatCount} •
                        {` ${r.departureTime} → ${r.arrivalTime}`}
                    </li>
                ))}

                {/* If there are no reservations, show the below message */}
                {reservations.length === 0 && <li>No reservations found.</li>}
            </ul>
        </div>
    );
}

// This lets other files import and use <ReservationManager />
export default ReservationManager;