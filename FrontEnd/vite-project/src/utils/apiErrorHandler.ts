/**
 * Centralized error handling utility for API responses
 * Provides consistent error message extraction and formatting
 */

/**
 * Handles API error responses by extracting error messages
 * @param response - The failed Response object from fetch
 * @returns Promise that resolves to an Error with an appropriate message
 */
export async function handleApiError(response: Response): Promise<Error> {
    let errorMessage = 'An error occurred';
    
    try {
        const error = await response.json();
        errorMessage = error.message || error.error || errorMessage;
    } catch {
        // If response is not JSON, use status-based fallback messages
        const statusMessages: Record<number, string> = {
            400: 'Invalid request',
            401: 'Unauthorized',
            403: 'Forbidden',
            404: 'Resource not found',
            409: 'Conflict - resource already exists',
            422: 'Validation error',
            500: 'Server error',
            502: 'Bad gateway',
            503: 'Service unavailable',
        };
        errorMessage = statusMessages[response.status] || `Request failed (${response.status})`;
    }
    
    return new Error(errorMessage);
}

