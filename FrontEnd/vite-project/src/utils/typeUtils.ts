import type { FieldErrors } from 'react-hook-form';
import type { CreateStationRequest, UpdateStationRequest } from '../types/Station';

export type FormRequest = CreateStationRequest | UpdateStationRequest;

export function getFieldError<T extends FormRequest>(
    errors: FieldErrors<T>,
    fieldKey: string
): unknown {
    // Safe type assertion: fieldKey is validated to be a key of T
    // Using Record<string, unknown> for maximum compatibility
    return (errors as Record<string, unknown>)[fieldKey];
}

