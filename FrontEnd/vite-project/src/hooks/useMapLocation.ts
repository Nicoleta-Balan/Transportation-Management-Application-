import { useCallback } from 'react';

import type { UseFormReturn } from 'react-hook-form';

import type { LocationFields } from '../types/Station';

type LocationFormOperations = {
    setValue: (field: keyof LocationFields, value: number | string, options?: { shouldValidate: boolean }) => void;
    trigger: (fields: (keyof LocationFields)[]) => Promise<boolean>;
};

export function useMapLocation<T extends LocationFields>(
    form: UseFormReturn<T>,
    setSelectedLocation: React.Dispatch<React.SetStateAction<{ lat: number; lng: number } | null>>
) {
    // Type-safe form operations - T extends LocationFields guarantees these fields exist
    const formOps = form as UseFormReturn<T> & LocationFormOperations;

    const handleLocationSelect = useCallback((location: { lat: number; lng: number; address: string }) => {
        const lat = Number(location.lat);
        const lng = Number(location.lng);
        
        // Type-safe field access - only accessing fields guaranteed by LocationFields
        formOps.setValue('latitude', lat, { shouldValidate: true });
        formOps.setValue('longitude', lng, { shouldValidate: true });
        formOps.setValue('address', location.address || '', { shouldValidate: true });
        setSelectedLocation({ lat, lng });
        formOps.trigger(['latitude', 'longitude', 'address']);
    }, [formOps, setSelectedLocation]);
    
    return { handleLocationSelect };
}

