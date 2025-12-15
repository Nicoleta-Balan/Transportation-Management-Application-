import { useCallback, useEffect } from 'react';
import { useForm } from 'react-hook-form';

import type { CreateRouteRequest } from '../types/Route';

import { routeApi } from '../services/routeApi';
import { DISTANCE_DECIMAL_PLACES_MULTIPLIER } from '../constants/calculationConstants';
import { buildRouteStops } from '../utils/routeStopBuilder';
import { validateRouteData, normalizeDescription } from '../utils/routeValidation';

import { useRouteStations } from './useRouteStations';
import { useRouteSegmentOverrides } from './useRouteSegmentOverrides';
import { useRouteCalculations } from './useRouteCalculations';
import { useCreateFormState } from './useFormState';
import { useFormSubmit } from './useFormSubmit';

export function useCreateRouteForm(
    loadRoutes: () => Promise<void>,
    onRouteCreated?: (routeId: number) => void
) {
    const { formState, setSubmitting, resetState, handleError } = useCreateFormState();

    const form = useForm<CreateRouteRequest>({
        mode: 'onChange',
        defaultValues: {
            vehicleClass: undefined,
            distance: 0,
            durationMinutes: 0,
            description: '',
            stops: [],
        },
    });

    const { register, handleSubmit, reset, watch, setValue, formState: { errors } } = form;
    const watchedVehicleClass = watch('vehicleClass');

    // Use specialized hooks for station management, calculations, and overrides
    const { selectedStations, handleStationSelect, clearSelection: clearStations } = useRouteStations();
    
    const {
        segmentOverrides,
        handleSegmentChange,
        clearSegmentOverrides,
    } = useRouteSegmentOverrides(selectedStations, setValue);

    const { calculatedDistance, calculatedDurationMinutes } = useRouteCalculations(selectedStations, segmentOverrides);

    // Auto-fill distance and duration when stations are selected (round to 1 decimal)
    useEffect(() => {
        if (calculatedDistance > 0 && calculatedDurationMinutes > 0) {
            setValue('distance', Math.round(calculatedDistance * DISTANCE_DECIMAL_PLACES_MULTIPLIER) / DISTANCE_DECIMAL_PLACES_MULTIPLIER, { shouldValidate: false });
            setValue('durationMinutes', calculatedDurationMinutes, { shouldValidate: false });
        }
    }, [calculatedDistance, calculatedDurationMinutes, setValue]);

    const clearSelection = useCallback(() => {
        clearStations();
        clearSegmentOverrides();
        form.setValue('stops', [], { shouldValidate: false });
        form.setValue('distance', 0, { shouldValidate: false });
        form.setValue('durationMinutes', 0, { shouldValidate: false });
    }, [form, clearStations, clearSegmentOverrides]);

    // Use generic form submit hook
    const { handleSubmit: handleFormSubmit } = useFormSubmit(
        setSubmitting,
        handleError,
        {
            submitFn: (data: CreateRouteRequest) => routeApi.createRoute(data),
            errorMessage: 'Failed to create route',
            validate: (data) => {
                const validation = validateRouteData(selectedStations, data.distance, data.durationMinutes);
                return validation.isValid ? null : (validation.error || 'Validation failed');
            },
            transformRequest: (data: CreateRouteRequest): CreateRouteRequest => {
                // Build stops array from selected stations and overrides
                const stops = buildRouteStops(selectedStations, segmentOverrides);
                
                // Build the request with stops
                return {
                    ...data,
                    stops,
                    description: normalizeDescription(data.description),
                };
            },
            onSuccess: (createdRoute) => {
                // Notify parent component about the created route
                if (createdRoute?.id && onRouteCreated) {
                    onRouteCreated(createdRoute.id);
                }
            },
            onReset: () => {
                reset();
                clearSelection();
                resetState();
            },
            onReload: loadRoutes,
        }
    );

    const onSubmit = useCallback(async (data: CreateRouteRequest) => {
        await handleFormSubmit(data);
    }, [handleFormSubmit]);

    return {
        register,
        handleSubmit,
        errors,
        submitting: formState.submitting,
        error: formState.error,
        selectedStations,
        segmentOverrides,
        handleStationSelect,
        handleSegmentChange,
        clearSelection,
        onSubmit,
        calculatedDistance,
        calculatedDurationMinutes,
        watchedVehicleClass,
    };
}