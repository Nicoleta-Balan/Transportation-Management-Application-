import { API_CONFIG } from '../config/api';

const SEAT_URL = `${API_CONFIG.BASE_URL}/api/seats`;

export interface OccupiedSeatsResponse {
    routeId: number;
    departureTime: string;
    occupiedSeats: string[];  // Seats from confirmed reservations
    heldSeats: string[];      // Seats temporarily held by other users
    myHeldSeats: string[];    // Seats held by the current session
}

export interface SeatHoldRequest {
    routeId: number;
    departureTime: string;
    seatNumbers: string[];
    sessionId: string;
}

export interface SeatHoldResponse {
    success: boolean;
    message: string;
    heldSeats: string[];
    failedSeats: string[];
    expiresAt: string;
}

// Generate or retrieve session ID for seat holds
export function getSessionId(): string {
    let sessionId = sessionStorage.getItem('booking_session_id');
    if (!sessionId) {
        sessionId = crypto.randomUUID();
        sessionStorage.setItem('booking_session_id', sessionId);
    }
    return sessionId;
}

export const seatApi = {
    async getOccupiedSeats(
        routeId: number,
        departureTime: string,
        sessionId: string
    ): Promise<OccupiedSeatsResponse> {
        const params = new URLSearchParams({
            routeId: routeId.toString(),
            departureTime,
            sessionId
        });

        const response = await fetch(`${SEAT_URL}/availability?${params}`);
        if (!response.ok) {
            throw new Error('Failed to fetch seat availability');
        }
        return response.json();
    },

    async holdSeats(request: SeatHoldRequest): Promise<SeatHoldResponse> {
        const token = localStorage.getItem('auth_token');
        const headers: HeadersInit = {
            'Content-Type': 'application/json',
        };
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        const response = await fetch(`${SEAT_URL}/hold`, {
            method: 'POST',
            headers,
            body: JSON.stringify(request),
        });

        if (!response.ok) {
            throw new Error('Failed to hold seats');
        }
        return response.json();
    },

    async releaseHolds(sessionId: string): Promise<void> {
        await fetch(`${SEAT_URL}/hold/${sessionId}`, {
            method: 'DELETE',
        });
    },
};
