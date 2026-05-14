import { API_CONFIG } from '../config/api';
import { getAuthHeaders } from './authApi';

const USER_URL = `${API_CONFIG.BASE_URL}/api/user`;
const RESERVATIONS_URL = `${API_CONFIG.BASE_URL}/api/reservations`;

export interface BookingResponse {
    id: number;
    routeId: number;
    originStation: string;
    destinationStation: string;
    passengerName: string;
    seatCount: number;
    selectedSeats: string | null;
    status: 'PENDING' | 'CONFIRMED' | 'CANCELLED';
    departureTime: string;
    arrivalTime: string;
    passengerCategory: 'ADULT' | 'CHILD' | 'SENIOR' | 'STUDENT';
    vehicleClass: 'STANDARD' | 'COACH' | 'MINI_BUS' | 'DOUBLE_DECKER';
    totalPrice: number | null;
    currency: string | null;
    createdAt: string;
}

export interface CreateReservationRequest {
    routeId: number;
    passengerName: string;
    seatCount: number;
    departureTime: string;
    arrivalTime: string;
    passengerCategory: 'ADULT' | 'CHILD' | 'SENIOR' | 'STUDENT';
    vehicleClass: 'STANDARD' | 'COACH' | 'MINI_BUS' | 'DOUBLE_DECKER';
    selectedSeats?: string[];
    sessionId?: string;
    totalPrice?: number;
    currency?: string;
}

export const bookingApi = {
    async createReservation(request: CreateReservationRequest): Promise<BookingResponse> {
        const response = await fetch(RESERVATIONS_URL, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                ...getAuthHeaders(),
            },
            body: JSON.stringify(request),
        });
        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || 'Failed to create reservation');
        }
        return response.json();
    },

    async getActiveBookings(): Promise<BookingResponse[]> {
        const response = await fetch(`${USER_URL}/bookings/active`, {
            headers: getAuthHeaders(),
        });
        if (!response.ok) {
            throw new Error('Failed to fetch active bookings');
        }
        return response.json();
    },

    async getBookingHistory(): Promise<BookingResponse[]> {
        const response = await fetch(`${USER_URL}/bookings/history`, {
            headers: getAuthHeaders(),
        });
        if (!response.ok) {
            throw new Error('Failed to fetch booking history');
        }
        return response.json();
    },
};
