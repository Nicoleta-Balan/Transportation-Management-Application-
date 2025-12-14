import type { UseFormRegister, FieldErrors } from 'react-hook-form';
import type { Station } from '../../../types/Station';
import type { CreateRouteRequest, UpdateRouteRequest } from '../../../types/Route';

import { VEHICLE_CLASS_OPTIONS } from '../../../constants/routeConstants';
import { vehicleClassValidation, distanceValidation, durationMinutesValidation, descriptionValidation } from '../../../validation/routeValidationSchemas';

import { useState, useEffect } from 'react';
import { FormInput } from '../../forms/FormInput';
import { FormSelect } from '../../forms/FormSelect';
import { FormTextarea } from '../../forms/FormTextarea';
import { IntermediaryStationsDisplay } from './IntermediaryStationsDisplay';
import { calculateDistance, calculateDuration } from '../../../utils/distanceCalculator';
import {
    MIN_ROUTE_DISTANCE_KM,
    MAX_ROUTE_DISTANCE_KM,
    MIN_ROUTE_DURATION_MINUTES,
    MAX_ROUTE_DURATION_MINUTES,
    MAX_ROUTE_DURATION_HOURS,
    MAX_ROUTE_DESCRIPTION_LENGTH,
    DISTANCE_DECIMAL_PLACES,
    MIN_SEGMENT_DISTANCE,
    DISTANCE_INPUT_STEP
} from '../../../constants/calculationConstants';
import './IntermediaryStationsDisplay.css';

interface RouteFormFieldsProps {
    register: UseFormRegister<CreateRouteRequest | UpdateRouteRequest>;
    errors: FieldErrors<CreateRouteRequest | UpdateRouteRequest>;
    startStation: Station | null;
    endStation: Station | null;
    selectedStations?: Station[]; // For intermediary stations display
    calculatedDistance: number; // Auto-calculated distance (for display/suggestion)
    calculatedDurationMinutes: number; // Auto-calculated duration in minutes (for display/suggestion)
    idPrefix?: string;
    segmentOverrides?: Map<number, { distance: number; duration: number }>;
    onSegmentChange?: (stationIndex: number, distance: number, duration: number) => void;
    editable?: boolean;
}

export function RouteFormFields({
    register,
    errors,
    startStation,
    endStation,
    selectedStations = [],
    calculatedDistance,
    calculatedDurationMinutes,
    idPrefix = '',
    segmentOverrides,
    onSegmentChange,
    editable = false,
}: RouteFormFieldsProps) {
    const prefix = idPrefix ? `${idPrefix}` : '';
    
    // Convert duration from minutes to hours for display
    const calculatedDurationHours = calculatedDurationMinutes / 60;
    
    // Calculate end station segment distance and duration
    const endStationIndex = selectedStations.length > 0 ? selectedStations.length - 1 : -1;
    const previousStationIndex = endStationIndex > 0 ? endStationIndex - 1 : -1;
    
    // Get the end station segment override if it exists
    const endStationOverride = endStationIndex > 0 && segmentOverrides ? segmentOverrides.get(endStationIndex) : undefined;
    
    // Calculate auto-calculated values for end station segment
    let autoCalculatedEndDistance = 0;
    let autoCalculatedEndDuration = 0;
    if (endStationIndex > 0 && previousStationIndex >= 0 && selectedStations.length > 1) {
        const prevStation = selectedStations[previousStationIndex];
        const endStationForCalc = selectedStations[endStationIndex];
        if (prevStation.latitude && prevStation.longitude && 
            endStationForCalc.latitude && endStationForCalc.longitude) {
            autoCalculatedEndDistance = calculateDistance(
                prevStation.latitude,
                prevStation.longitude,
                endStationForCalc.latitude,
                endStationForCalc.longitude
            );
            const durationHours = calculateDuration(autoCalculatedEndDistance);
            autoCalculatedEndDuration = Math.round(durationHours * 60);
        }
    }
    
    // Get current values (override or auto-calculated)
    const endSegmentDistance = endStationOverride 
        ? endStationOverride.distance 
        : autoCalculatedEndDistance;
    const endSegmentDuration = endStationOverride 
        ? endStationOverride.duration 
        : autoCalculatedEndDuration;
    
    // State for editing end station segment
    const [isEditingEndSegment, setIsEditingEndSegment] = useState(false);
    const [tempEndDistance, setTempEndDistance] = useState(Math.round(endSegmentDistance * 10) / 10);
    const [tempEndDuration, setTempEndDuration] = useState(endSegmentDuration);
    
    // Update temp values when segment changes (only when not editing)
    useEffect(() => {
        if (!isEditingEndSegment && endStationIndex > 0) {
            const currentEndDistance = Math.round(endSegmentDistance * 10) / 10;
            const currentEndDuration = endSegmentDuration;
            setTempEndDistance(currentEndDistance);
            setTempEndDuration(currentEndDuration);
        }
    }, [endSegmentDistance, endSegmentDuration, endStationIndex, isEditingEndSegment]);
    
    // Format duration helper
    const formatDuration = (minutes: number): string => {
        const hours = Math.floor(minutes / 60);
        const mins = minutes % 60;
        if (hours === 0) {
            return `0h ${mins} min`;
        }
        if (mins === 0) {
            return `${hours}h`;
        }
        return `${hours}h ${mins} min`;
    };
    
    // Handle end segment save
    const handleEndSegmentSave = () => {
        if (endStationIndex > 0 && onSegmentChange) {
            const roundedDistance = Math.round(tempEndDistance * 10) / 10;
            onSegmentChange(endStationIndex, roundedDistance, tempEndDuration);
        }
        setIsEditingEndSegment(false);
    };
    
    // Handle end segment cancel
    const handleEndSegmentCancel = () => {
        setTempEndDistance(Math.round(endSegmentDistance * 10) / 10);
        setTempEndDuration(endSegmentDuration);
        setIsEditingEndSegment(false);
    };
    
    return (
        <>
            {/* Start Station (read-only) */}
            <FormInput
                id={`${prefix}startStation`}
                label="Start Station"
                value={startStation?.name || ''}
                readOnly={true}
                helpText={startStation?.address || "Select a station from the map"}
            />

            {/* Intermediary Stations Display */}
            {selectedStations.length > 2 && (
                <IntermediaryStationsDisplay 
                    selectedStations={selectedStations}
                    segmentOverrides={segmentOverrides}
                    onSegmentChange={onSegmentChange}
                    editable={editable}
                />
            )}

            {/* End Station (read-only) */}
            <FormInput
                id={`${prefix}endStation`}
                label="End Station"
                value={endStation?.name || ''}
                readOnly={true}
                helpText={endStation?.address || "Select a station from the map"}
            />
            
            {/* End Station Segment Metrics (editable when stations selected) */}
            {endStationIndex > 0 && selectedStations.length >= 2 && (
                <div className="end-station-segment">
                    {isEditingEndSegment && editable ? (
                        <div className="segment-metrics-edit">
                            <label>
                                Distance from Previous (km):
                                <input
                                    type="number"
                                    step={DISTANCE_INPUT_STEP}
                                    min={MIN_SEGMENT_DISTANCE}
                                    max={MAX_ROUTE_DISTANCE_KM}
                                    value={tempEndDistance}
                                    onChange={(e) => {
                                        const value = parseFloat(e.target.value) || 0;
                                        const rounded = Math.round(value * 10) / 10;
                                        setTempEndDistance(rounded);
                                    }}
                                    className="segment-input"
                                />
                                <span className="help-text">Auto-calculated: {autoCalculatedEndDistance.toFixed(DISTANCE_DECIMAL_PLACES)} km</span>
                            </label>
                            <label>
                                Duration from Previous (min):
                                <input
                                    type="number"
                                    min={MIN_ROUTE_DURATION_MINUTES}
                                    max={MAX_ROUTE_DURATION_MINUTES}
                                    value={tempEndDuration}
                                    onChange={(e) => setTempEndDuration(parseInt(e.target.value) || 0)}
                                    className="segment-input"
                                />
                                <span className="help-text">Auto-calculated: {formatDuration(autoCalculatedEndDuration)}</span>
                            </label>
                            <div className="segment-actions">
                                <button
                                    type="button"
                                    onClick={handleEndSegmentSave}
                                    className="btn-save-segment"
                                >
                                    Save
                                </button>
                                <button
                                    type="button"
                                    onClick={handleEndSegmentCancel}
                                    className="btn-cancel-segment"
                                >
                                    Cancel
                                </button>
                            </div>
                        </div>
                    ) : (
                        <div className="segment-metrics">
                            <span className="segment-distance">
                                Distance from Previous: {endSegmentDistance.toFixed(DISTANCE_DECIMAL_PLACES)} km
                            </span>
                            <span className="segment-duration">
                                Duration from Previous: {formatDuration(endSegmentDuration)}
                            </span>
                            {editable && (
                                <button
                                    type="button"
                                    onClick={() => {
                                        setTempEndDistance(Math.round(endSegmentDistance * 10) / 10);
                                        setTempEndDuration(endSegmentDuration);
                                        setIsEditingEndSegment(true);
                                    }}
                                    className="btn-edit-segment"
                                >
                                    Edit
                                </button>
                            )}
                        </div>
                    )}
                </div>
            )}

            {/* Vehicle Class */}
            <FormSelect
                id={`${prefix}vehicleClass`}
                label="Vehicle Class"
                required={true}
                options={VEHICLE_CLASS_OPTIONS}
                placeholder="Select vehicle class..."
                register={register('vehicleClass', vehicleClassValidation)}
                error={errors.vehicleClass}
            />

            {/* Description */}
            <FormTextarea
                id={`${prefix}description`}
                label="Description"
                placeholder={`Enter route description (optional, max ${MAX_ROUTE_DESCRIPTION_LENGTH} characters)...`}
                rows={2}
                maxLength={MAX_ROUTE_DESCRIPTION_LENGTH}
                register={register('description', descriptionValidation)}
                error={errors.description}
            />

            {/* Distance (editable) */}
            <FormInput
                id={`${prefix}distance`}
                label="Total Distance (km)"
                type="number"
                required={true}
                register={register('distance', distanceValidation)}
                error={errors.distance}
                placeholder={calculatedDistance > 0 ? calculatedDistance.toFixed(DISTANCE_DECIMAL_PLACES) : ''}
                helpText={`Auto-calculated: ${calculatedDistance > 0 ? calculatedDistance.toFixed(DISTANCE_DECIMAL_PLACES) : 'N/A'} km (min: ${MIN_ROUTE_DISTANCE_KM}, max: ${MAX_ROUTE_DISTANCE_KM})`}
            />

            {/* Duration (editable, in minutes) */}
            <FormInput
                id={`${prefix}durationMinutes`}
                label="Total Duration (minutes)"
                type="number"
                required={true}
                register={register('durationMinutes', durationMinutesValidation)}
                error={errors.durationMinutes}
                placeholder={calculatedDurationMinutes > 0 ? calculatedDurationMinutes.toString() : ''}
                helpText={`Auto-calculated: ${calculatedDurationMinutes > 0 ? `${calculatedDurationMinutes} min (${calculatedDurationHours.toFixed(DISTANCE_DECIMAL_PLACES)} h)` : 'N/A'} (min: ${MIN_ROUTE_DURATION_MINUTES} min, max: ${MAX_ROUTE_DURATION_MINUTES} min / ${MAX_ROUTE_DURATION_HOURS} h)`}
            />
        </>
    );
}

