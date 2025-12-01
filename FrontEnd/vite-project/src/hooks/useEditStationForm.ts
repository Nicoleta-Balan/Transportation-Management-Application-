import { useState, useCallback, useEffect } from 'react';

import { useForm } from 'react-hook-form';

import type { Station, UpdateStationRequest } from '../types/Station';

import { stationApi } from '../services/stationApi';

import { useMapLocation } from './useMapLocation';
import { useLocationState } from './useLocationState';

import { getErrorMessage } from '../utils/errorUtils';

interface EditFormState {
    editingStation: Station | null;
    error: string | null;
    updating: boolean;
}

export function useEditStationForm(
    loadStations: () => Promise<void>
) {
    const [editFormState, setEditFormState] = useState<EditFormState>({
        editingStation: null,
        error: null,
        updating: false,
    });

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
        if (editFormState.editingStation) {
            baseHandleLocationSelect(location);
        }
    }, [editFormState.editingStation, baseHandleLocationSelect]);

    const handleEditClick = useCallback((station: Station) => {
        // Pre-populate the edit form with current station data
        setEditValue('description', station.description || '');
        setEditValue('address', station.address);
        setEditValue('latitude', station.latitude);
        setEditValue('longitude', station.longitude);
        setEditValue('status', station.status);
        // Set initial state for edit form
        setEditFormState({
            editingStation: station,
            error: null,
            updating: false,
        });
        // Set initial location for edit form
        setEditSelectedLocation({ lat: station.latitude, lng: station.longitude });
    }, [setEditValue, setEditSelectedLocation]);

    const handleEditSubmit = useCallback(async (data: UpdateStationRequest) => {
        if (!editFormState.editingStation) return;

        setEditFormState(prev => ({ ...prev, updating: true, error: null }));

        try {
            await stationApi.updateStation(editFormState.editingStation.id, data);
            // Success: close edit mode and refresh list
            resetForm();
            setEditFormState({ editingStation: null, error: null, updating: false });
            await loadStations(); // Refresh the stations list
        } catch (err) {
            let errorMessage = getErrorMessage(err, 'Failed to update station');
            // Check if it's a duplicate address error
            if (err instanceof Error && err.message.includes('address') && err.message.includes('already exists')) {
                errorMessage = 'This address is already used by another station. Please select a different location.';
            }
            setEditFormState(prev => ({ ...prev, updating: false, error: errorMessage }));
        }
    }, [editFormState.editingStation, resetForm, loadStations]);

    const handleCancelEdit = useCallback(() => {
        resetForm();
        setEditFormState({ editingStation: null, error: null, updating: false });
    }, [resetForm]);

    // Reset edit form when editingStation changes
    useEffect(() => {
        if (!editFormState.editingStation) {
            resetForm();
        }
    }, [editFormState.editingStation, resetForm]);

    return {
        // Form methods
        registerEdit,
        handleEditSubmitForm,
        watchEdit,
        editErrors,
        isEditValid,
        updating: editFormState.updating,
        editError: editFormState.error,
        editingStation: editFormState.editingStation,
        editSelectedLocation,
        handleEditMapLocationSelect,
        handleEditClick,
        handleEditSubmit,
        handleCancelEdit,
    };
}

