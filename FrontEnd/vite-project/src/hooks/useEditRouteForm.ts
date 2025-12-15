import { useCallback } from 'react';
import { useForm } from 'react-hook-form';

import type { Route, UpdateRouteRequest } from '../types/Route';

import { routeApi } from '../services/routeApi';
import { buildRouteStops } from '../utils/routeStopBuilder';
import { validateRouteData, normalizeDescription } from '../utils/routeValidation';

import { useRouteStations } from './useRouteStations';
import { useRouteSegmentOverrides } from './useRouteSegmentOverrides';
import { useRouteCalculations } from './useRouteCalculations';
import { useEditFormState } from './useFormState';
import { useFormSubmit } from './useFormSubmit';

export function useEditRouteForm(
    loadRoutes: () => Promise<void>
) {
    const { formState, setEditingResource, setUpdating, resetState, handleError } = useEditFormState<Route>();

    const form = useForm<UpdateRouteRequest>({
        mode: 'onChange',
        defaultValues: {
            vehicleClass: undefined,
            distance: undefined,
            durationMinutes: undefined,
            description: '',
            stops: [],
        },
    });

    const { register, handleSubmit, reset, watch, setValue, formState: { errors } } = form;
    const watchedVehicleClass = watch('vehicleClass');

    // Use specialized hooks for station management, calculations, and overrides
    const { selectedStations, handleStationSelect: selectStation, clearSelection, setStations } = useRouteStations();
    
    const {
        segmentOverrides,
        segmentOverridesRef,
        handleSegmentChange,
        clearSegmentOverrides,
        setSegmentOverridesFromRoute,
    } = useRouteSegmentOverrides(selectedStations, setValue);

    const { calculatedDistance, calculatedDurationMinutes } = useRouteCalculations(selectedStations, segmentOverrides);

    const handleStationSelect = useCallback((station: any) => {
        if (!formState.editingResource) {
            return;
        }
        selectStation(station);
    }, [formState.editingResource, selectStation]);

    const handleEditClick = useCallback((route: Route) => {
        // Extract stations from route stops
        const stations = route.routeStops
            ? route.routeStops.map(stop => stop.station)
            : route.originStation && route.destinationStation
                ? [route.originStation, route.destinationStation]
                : [];

        // Pre-populate the edit form with current route data
        setValue('vehicleClass', route.vehicleClass);
        setValue('distance', route.distance);
        setValue('durationMinutes', route.durationMinutes);
        setValue('description', route.description || '');
        
        // Set selected stations for visualization
        setStations(stations);
        
        // Populate segmentOverrides with existing route stop distances and durations
        if (route.routeStops) {
            setSegmentOverridesFromRoute(route.routeStops);
        }
        
        // Set editing state
        setEditingResource(route);
    }, [setValue, setStations, setSegmentOverridesFromRoute]);

    const resetForm = useCallback(() => {
        reset();
        clearSelection();
        clearSegmentOverrides();
        resetState();
    }, [reset, clearSelection, clearSegmentOverrides, resetState]);

    // Use generic form submit hook
    const { handleSubmit: handleFormSubmit } = useFormSubmit(
        setUpdating,
        handleError,
        {
            submitFn: (data: UpdateRouteRequest) => {
                if (!formState.editingResource) {
                    throw new Error('No route is being edited');
                }
                return routeApi.updateRoute(formState.editingResource.id, data);
            },
            errorMessage: 'Failed to update route',
            validate: (data) => {
                if (!formState.editingResource) {
                    return 'No route is being edited';
                }
                const validation = validateRouteData(selectedStations, data.distance, data.durationMinutes);
                return validation.isValid ? null : (validation.error || 'Validation failed');
            },
            transformRequest: (data: UpdateRouteRequest): UpdateRouteRequest => {
                // Build stops array from selected stations, using overrides when available
                // Use the ref to get the LATEST segmentOverrides (fixes stale closure bug)
                const currentOverrides = segmentOverridesRef.current;
                const stops = buildRouteStops(selectedStations, currentOverrides);
                
                // Build the update request with stops
                return {
                    ...data,
                    stops,
                    description: normalizeDescription(data.description),
                };
            },
            onReset: resetForm,
            onReload: loadRoutes,
        }
    );

    const onSubmit = useCallback(async (data: UpdateRouteRequest) => {
        await handleFormSubmit(data);
    }, [handleFormSubmit]);
    // Note: selectedStations and segmentOverrides are captured in the config functions,
    // and segmentOverrides uses a ref to avoid stale closures

    return {
        register,
        handleSubmit,
        errors,
        updating: formState.updating,
        error: formState.error,
        editingRoute: formState.editingResource,
        selectedStations,
        segmentOverrides,
        handleStationSelect,
        handleEditClick,
        handleSegmentChange,
        resetForm,
        onSubmit,
        calculatedDistance,
        calculatedDurationMinutes,
        watchedVehicleClass,
    };
}