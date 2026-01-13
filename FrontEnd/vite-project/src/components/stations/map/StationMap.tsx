import { useEffect, useState, useRef } from 'react';

import type { Station } from '../../../types/Station';

import { MAP_CONFIG } from '../../../constants/stationConstants';

import { BaseMap } from '../../common/BaseMap';
import { MapClickHandler } from './MapClickHandler';
import { StationMarkers } from './StationMarkers';

interface StationMapProps {
    onLocationSelect: (location: { lat: number; lng: number; address: string }) => void;
    selectedLocation?: { lat: number; lng: number } | null;
    existingStations?: Station[];
    height?: string;
    initialLocation?: { lat: number; lng: number };
    editingStationId?: number | null;
    onCreateFormLocationSelect?: (location: { lat: number; lng: number; address: string }) => void;
    isCreateFormExpanded?: boolean;
    createFormSelectedLocation?: { lat: number; lng: number } | null;
}

// Photon API Search Function (Better than raw Nominatim)
async function searchAddress(query: string) {
    if (query.length < 3) return [];
    try {
        // Photon API by Komoot - excellent for autocomplete/fuzzy search
        const response = await fetch(
            `https://photon.komoot.io/api/?q=${encodeURIComponent(query)}&limit=5&lang=en`
        );
        const data = await response.json();
        
        return data.features.map((item: any) => {
            const props = item.properties;
            // Construct a readable address
            const addressParts = [
                props.name,
                props.street,
                props.housenumber,
                props.city,
                props.state,
                props.country
            ].filter(Boolean); // Remove null/undefined
            
            return {
                id: props.osm_id || Math.random(),
                name: props.name || props.street || props.city, // Main title
                fullAddress: addressParts.join(', '), // Subtitle
                lat: item.geometry.coordinates[1], // GeoJSON is [lon, lat]
                lng: item.geometry.coordinates[0]
            };
        });
    } catch (error) {
        console.error("Error searching address:", error);
        return [];
    }
}

export default function StationMap({
                                       onLocationSelect,
                                       selectedLocation,
                                       existingStations = [],
                                       height = MAP_CONFIG.MAP_HEIGHT,
                                       initialLocation,
                                       editingStationId,
                                       onCreateFormLocationSelect,
                                       isCreateFormExpanded = false,
                                       createFormSelectedLocation,
                                   }: StationMapProps) {
    const [center, setCenter] = useState<[number, number] | undefined>(
        initialLocation ? [initialLocation.lat, initialLocation.lng] : undefined
    );
    const [suggestions, setSuggestions] = useState<any[]>([]);
    const [inputValue, setInputValue] = useState("");
    const [isSearching, setIsSearching] = useState(false);
    const [showSuggestions, setShowSuggestions] = useState(false);
    
    const debounceTimeoutRef = useRef<NodeJS.Timeout | null>(null);
    const wrapperRef = useRef<HTMLDivElement>(null);

    // Update center when location is selected
    useEffect(() => {
        if (selectedLocation) {
            setCenter([selectedLocation.lat, selectedLocation.lng]);
        }
    }, [selectedLocation]);

    // Close suggestions when clicking outside
    useEffect(() => {
        function handleClickOutside(event: MouseEvent) {
            if (wrapperRef.current && !wrapperRef.current.contains(event.target as Node)) {
                setShowSuggestions(false);
            }
        }
        document.addEventListener("mousedown", handleClickOutside);
        return () => document.removeEventListener("mousedown", handleClickOutside);
    }, []);

    const handleAddressSearch = (e: React.ChangeEvent<HTMLInputElement>) => {
        const query = e.target.value;
        setInputValue(query);
        setShowSuggestions(true);

        if (debounceTimeoutRef.current) {
            clearTimeout(debounceTimeoutRef.current);
        }

        if (query.length <= 2) {
            setSuggestions([]);
            setIsSearching(false);
            return;
        }

        setIsSearching(true);

        // Faster debounce (300ms) for snappier feel
        debounceTimeoutRef.current = setTimeout(async () => {
            const results = await searchAddress(query);
            setSuggestions(results);
            setIsSearching(false);
        }, 300);
    };

    const handleSuggestionSelect = (suggestion: any) => {
        const location = {
            lat: suggestion.lat,
            lng: suggestion.lng,
            address: suggestion.fullAddress
        };
        
        setCenter([location.lat, location.lng]);
        setInputValue(suggestion.name); 
        setShowSuggestions(false);
        
        if (editingStationId) {
            onLocationSelect(location);
        } else if (onCreateFormLocationSelect) {
            onCreateFormLocationSelect(location);
        } else {
            onLocationSelect(location);
        }
    };

    return (
        <div style={{ position: 'relative' }}>
            {/* Professional Search Bar Overlay */}
            <div ref={wrapperRef} style={{
                position: 'absolute',
                top: '10px',
                left: '50px', 
                right: '10px',
                zIndex: 1000,
                maxWidth: '400px',
            }}>
                 <div className="map-search-box" style={{ 
                     position: 'relative',
                     boxShadow: '0 2px 6px rgba(0,0,0,0.3)',
                     borderRadius: '4px',
                     backgroundColor: 'white'
                 }}>
                    <div style={{ display: 'flex', alignItems: 'center', padding: '0 10px' }}>
                        <span style={{ color: '#666', marginRight: '5px' }}>🔍</span>
                        <input
                            type="text"
                            placeholder="Search address (e.g. Iasi)..."
                            value={inputValue}
                            onChange={handleAddressSearch}
                            onFocus={() => setShowSuggestions(true)}
                            style={{
                                width: '100%',
                                padding: '12px 0',
                                border: 'none',
                                outline: 'none',
                                fontSize: '14px'
                            }}
                        />
                        {inputValue && (
                            <button 
                                onClick={() => {
                                    setInputValue('');
                                    setSuggestions([]);
                                    setShowSuggestions(false);
                                }}
                                style={{
                                    background: 'none',
                                    border: 'none',
                                    color: '#999',
                                    cursor: 'pointer',
                                    fontSize: '16px',
                                    padding: '0 5px'
                                }}
                            >
                                ×
                            </button>
                        )}
                    </div>

                    {showSuggestions && (suggestions.length > 0 || isSearching) && (
                        <ul style={{
                            position: 'absolute',
                            top: '100%',
                            left: 0,
                            right: 0,
                            backgroundColor: 'white',
                            listStyle: 'none',
                            margin: '5px 0 0 0',
                            padding: 0,
                            boxShadow: '0 4px 6px rgba(0,0,0,0.1)',
                            maxHeight: '300px',
                            overflowY: 'auto',
                            borderRadius: '4px'
                        }}>
                            {isSearching && (
                                <li style={{ padding: '10px', color: '#666', fontSize: '13px' }}>
                                    Searching...
                                </li>
                            )}
                            
                            {!isSearching && suggestions.map((s) => (
                                <li 
                                    key={s.id}
                                    onClick={() => handleSuggestionSelect(s)}
                                    style={{
                                        padding: '10px 15px',
                                        cursor: 'pointer',
                                        borderBottom: '1px solid #f0f0f0',
                                        fontSize: '13px',
                                        lineHeight: '1.4'
                                    }}
                                    onMouseOver={(e) => e.currentTarget.style.backgroundColor = '#f5f5f5'}
                                    onMouseOut={(e) => e.currentTarget.style.backgroundColor = 'white'}
                                >
                                    <div style={{ fontWeight: '600', color: '#333' }}>{s.name}</div>
                                    <div style={{ color: '#777', fontSize: '12px', marginTop: '2px' }}>
                                        {s.fullAddress}
                                    </div>
                                </li>
                            ))}
                        </ul>
                    )}
                </div>
            </div>

            <BaseMap
                height={height}
                initialCenter={initialLocation ? [initialLocation.lat, initialLocation.lng] : undefined}
                center={center}
                useGeolocation={!initialLocation && !selectedLocation}
            >
                <MapClickHandler 
                    onLocationSelect={editingStationId && onCreateFormLocationSelect ? onCreateFormLocationSelect : onLocationSelect} 
                />
                <StationMarkers
                    stations={existingStations}
                    editingStationId={editingStationId}
                    selectedLocation={selectedLocation}
                    onLocationSelect={onLocationSelect}
                    isCreateFormExpanded={isCreateFormExpanded}
                    onCreateFormLocationSelect={onCreateFormLocationSelect}
                    createFormSelectedLocation={createFormSelectedLocation}
                />
            </BaseMap>
        </div>
    );
}
