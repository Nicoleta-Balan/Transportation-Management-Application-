import { useCallback, useEffect } from 'react';

import { useForm } from 'react-hook-form';

import type { Station, UpdateStationRequest } from '../types/Station';

import { stationApi } from '../services/stationApi';

import { useMapLocation } from './useMapLocation';
import { useLocationState } from './useLocationState';
import { useEditFormState } from './useFormState';
import { useFormSubmit } from './useFormSubmit';

export function useEditStationForm(
    loadStations: () => Promise<void>
) {
    const { formState, setEditingResource, setUpdating, resetState, handleError } = useEditFormState<Station>();

    // Use dedicated location state hook
    const { location: editSelectedLocation, setLocation: setEditSelectedLocation, resetLocation } = useLocationState();

    const form = useForm<UpdateStationRequest>({
        mode: 'onChange',
    });

    const {
        register: registerEdit,
        handleSubmit: handleEditSubmitForm,
        formState: { errors: editErrors, isValid: isEditValid },
        watch: watchEdit,
        setValue: setEditValue,
        reset: resetEdit,
    } = form;

    // Use map location handler directly
    // setEditSelectedLocation from useLocationState handles both direct values and updater functions
    // Type assertion is safe because UpdateStationRequest extends LocationFields
    // and useMapLocation only accesses fields defined in LocationFields
    // Using a more specific type assertion instead of 'as any'
    const { handleLocationSelect: baseHandleLocationSelect } = useMapLocation(
        form as ReturnType<typeof useForm<UpdateStationRequest>>,
        setEditSelectedLocation
    );

    const resetForm = useCallback(() => {
        resetEdit();
        resetLocation();
    }, [resetEdit, resetLocation]);

    const handleEditMapLocationSelect = useCallback((location: { lat: number; lng: number; address: string }) => {
        if (formState.editingResource) {
            baseHandleLocationSelect(location);
        }
    }, [formState.editingResource, baseHandleLocationSelect]);

    const handleEditClick = useCallback((station: Station) => {
        // Pre-populate the edit form with current station data
        setEditValue('description', station.description || '');
        setEditValue('address', station.address);
        setEditValue('latitude', station.latitude);
        setEditValue('longitude', station.longitude);
        setEditValue('status', station.status);
        // Set initial state for edit form
        setEditingResource(station);
        // Set initial location for edit form
        setEditSelectedLocation({ lat: station.latitude, lng: station.longitude });
    }, [setEditValue, setEditSelectedLocation]);

    // Use generic form submit hook
    const { handleSubmit: handleFormSubmit } = useFormSubmit(
        setUpdating,
        handleError,
        {
            submitFn: (data: UpdateStationRequest) => {
                if (!formState.editingResource) {
                    throw new Error('No station is being edited');
                }
                return stationApi.updateStation(formState.editingResource.id, data);
            },
            errorMessage: 'Failed to update station',
            validate: () => {
                if (!formState.editingResource) {
                    return 'No station is being edited';
                }
                return null;
            },
            onReset: () => {
                resetForm();
                resetState();
            },
            onReload: loadStations,
            onError: (err, defaultHandler) => {
                // Check if it's a duplicate address error
                if (err instanceof Error && err.message.includes('address') && err.message.includes('already exists')) {
                    handleError(new Error('This address is already used by another station. Please select a different location.'), 'Validation failed');
                } else {
                    defaultHandler(err, 'Failed to update station');
                }
            },
        }
    );

    const handleEditSubmit = useCallback(async (data: UpdateStationRequest) => {
        await handleFormSubmit(data);
    }, [handleFormSubmit]);

    const handleCancelEdit = useCallback(() => {
        resetForm();
        resetState();
    }, [resetForm, resetState]);

    // Reset edit form when editingStation changes
    useEffect(() => {
        if (!formState.editingResource) {
            resetForm();
        }
    }, [formState.editingResource, resetForm]);

    return {
        // Form methods
        registerEdit,
        handleEditSubmitForm,
        watchEdit,
        editErrors,
        isEditValid,
        updating: formState.updating,
        editError: formState.error,
        editingStation: formState.editingResource,
        editSelectedLocation,
        handleEditMapLocationSelect,
        handleEditClick,
        handleEditSubmit,
        handleCancelEdit,
    };
}

