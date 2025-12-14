import { useCallback } from 'react';

import { useForm, type RegisterOptions } from 'react-hook-form';

import type { Station, CreateStationRequest } from '../types/Station';
import { StationStatus } from '../types/Station';

import { VALIDATION_RULES } from '../constants/stationConstants';

import { stationApi } from '../services/stationApi';

import { useMapLocation } from './useMapLocation';
import { useLocationState } from './useLocationState';
import { useCreateFormState } from './useFormState';
import { useFormSubmit } from './useFormSubmit';

export function useCreateStationForm(
    setStations: React.Dispatch<React.SetStateAction<Station[]>>,
    existingStations: Station[] = [],
    onStationCreated?: (stationId: number) => void
) {
    const { formState, setSubmitting, resetState, handleError } = useCreateFormState();

    // Use dedicated location state hook
    const { location: selectedLocation, setLocation: setSelectedLocation, resetLocation } = useLocationState();

    // Validation function to check for duplicate station names
    const validateUniqueName = useCallback((name: string): string | true => {
        if (!name || name.trim().length === 0) {
            return true; // Let required validation handle empty names
        }
        const trimmedName = name.trim();
        const isDuplicate = existingStations.some(
            station => station.name.toLowerCase() === trimmedName.toLowerCase()
        );
        return isDuplicate ? 'A station with this name already exists' : true;
    }, [existingStations]);

    const form = useForm<CreateStationRequest>({
        mode: 'onChange',
        defaultValues: {
            name: '',
            description: '',
            status: StationStatus.ACTIVE,
            address: '',
            latitude: undefined,
            longitude: undefined,
        },
    });

    const { register, handleSubmit, watch, reset, formState: { errors, isValid } } = form;

    // Watched values
    const watchedAddress = watch('address');
    const watchedName = watch('name');
    const watchedStatus = watch('status');
    
    // Show address error only if other mandatory fields are filled
    const isNameValid = watchedName && watchedName.length >= VALIDATION_RULES.NAME_MIN_LENGTH;
    const isAddressEmpty = !watchedAddress || watchedAddress.trim() === '';
    const shouldShowAddressError = Boolean(isNameValid && watchedStatus && (errors.address || isAddressEmpty));

    const { handleLocationSelect: handleMapLocationSelect } = useMapLocation(
        form as ReturnType<typeof useForm<CreateStationRequest>>,
        setSelectedLocation
    );

    // Use generic form submit hook
    const { handleSubmit: handleFormSubmit } = useFormSubmit(
        setSubmitting,
        handleError,
        {
            submitFn: (data: CreateStationRequest) => stationApi.createStation(data),
            errorMessage: 'Failed to create station',
            onSuccess: (newStation) => {
                setStations(prev => [...prev, newStation]);
                // Call the callback with the new station ID
                if (onStationCreated && newStation.id) {
                    onStationCreated(newStation.id);
                }
            },
            onReset: () => {
                reset();
                resetLocation();
                resetState();
            },
        }
    );

    const onSubmit = useCallback(async (data: CreateStationRequest) => {
        await handleFormSubmit(data);
    }, [handleFormSubmit]);

    // Create a custom register function that adds duplicate validation for name field
    // Uses RegisterOptions for better type safety, with type assertion for merged options
    // The assertion is safe because we're only modifying the validate function
    const customRegister = useCallback((
        fieldName: keyof CreateStationRequest,
        options?: RegisterOptions<CreateStationRequest, keyof CreateStationRequest>
    ) => {
        if (fieldName === 'name' && options) {
            // Merge duplicate validation with existing options
            const existingValidate = options.validate;
            const mergedOptions = {
                ...options,
                validate: (value: string) => {
                    // Run existing validation if present (handle both function and Record types)
                    if (existingValidate) {
                        if (typeof existingValidate === 'function') {
                            const existingResult = existingValidate(value, {} as CreateStationRequest);
                            if (existingResult !== true) {
                                return existingResult;
                            }
                        } else {
                            // Handle Record<string, Validate> case
                            for (const key in existingValidate) {
                                const validator = existingValidate[key];
                                if (typeof validator === 'function') {
                                    const result = validator(value, {} as CreateStationRequest);
                                    if (result !== true) {
                                        return result;
                                    }
                                }
                            }
                        }
                    }
                    // Then check for duplicates
                    return validateUniqueName(value);
                },
            } as RegisterOptions<CreateStationRequest, 'name'>;
            return register(fieldName, mergedOptions);
        }
        return register(fieldName, options);
    }, [register, validateUniqueName]);

    return {
        // Form methods
        register: customRegister as typeof register,
        handleSubmit,
        errors,
        isValid,
        submitting: formState.submitting,
        error: formState.error,
        selectedLocation,
        handleMapLocationSelect,
        onSubmit,
        watchedAddress,
        watchedName,
        watchedStatus,
        shouldShowAddressError,
    };
}

