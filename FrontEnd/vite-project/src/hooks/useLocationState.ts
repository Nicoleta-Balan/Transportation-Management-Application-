import { useState, useCallback } from 'react';

export function useLocationState(initialLocation: { lat: number; lng: number } | null = null) {
    const [location, setLocation] = useState<{ lat: number; lng: number } | null>(initialLocation);

    const setLocationState = useCallback(
        (newLocation: React.SetStateAction<{ lat: number; lng: number } | null>) => {
            setLocation(newLocation);
        },
        []
    );

    const resetLocation = useCallback(() => {
        setLocation(null);
    }, []);

    return {
        location,
        setLocation: setLocationState,
        resetLocation,
    };
}

