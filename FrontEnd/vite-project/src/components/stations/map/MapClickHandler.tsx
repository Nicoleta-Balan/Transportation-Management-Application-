import { useMapEvents } from 'react-leaflet';

import { reverseGeocode } from '../../../utils/geocoding';

interface MapClickHandlerProps {
    onLocationSelect: (location: { lat: number; lng: number; address: string }) => void;
}

export function MapClickHandler({ onLocationSelect }: MapClickHandlerProps) {
    useMapEvents({
        click: (e) => {
            const { lat, lng } = e.latlng;
            reverseGeocode(lat, lng).then((address) => {
                onLocationSelect({ lat, lng, address });
            });
        },
    });
    return null;
}

