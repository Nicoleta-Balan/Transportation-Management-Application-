import { useState, useCallback } from 'react';
import { getErrorMessage } from '../utils/errorUtils';

export interface CreateFormState {
    submitting: boolean;
    error: string | null;
}

export interface EditFormState<T> {
    editingResource: T | null;
    updating: boolean;
    error: string | null;
}

export function useCreateFormState() {
    const [formState, setFormState] = useState<CreateFormState>({
        submitting: false,
        error: null,
    });

    const setSubmitting = useCallback((submitting: boolean) => {
        setFormState(prev => ({ ...prev, submitting }));
    }, []);

    const setError = useCallback((error: string | null) => {
        setFormState(prev => ({ ...prev, error }));
    }, []);

    const resetState = useCallback(() => {
        setFormState({ submitting: false, error: null });
    }, []);

    const handleError = useCallback((err: unknown, defaultMessage: string) => {
        setFormState(prev => ({
            ...prev,
            submitting: false,
            error: getErrorMessage(err, defaultMessage),
        }));
    }, []);

    return {
        formState,
        setSubmitting,
        setError,
        resetState,
        handleError,
    };
}

export function useEditFormState<T>(initialResource: T | null = null) {
    const [formState, setFormState] = useState<EditFormState<T>>({
        editingResource: initialResource,
        updating: false,
        error: null,
    });

    const setEditingResource = useCallback((resource: T | null) => {
        setFormState(prev => ({
            ...prev,
            editingResource: resource,
            error: null,
            updating: false,
        }));
    }, []);

    const setUpdating = useCallback((updating: boolean) => {
        setFormState(prev => ({ ...prev, updating }));
    }, []);

    const setError = useCallback((error: string | null) => {
        setFormState(prev => ({ ...prev, error }));
    }, []);

    const resetState = useCallback(() => {
        setFormState({
            editingResource: null,
            updating: false,
            error: null,
        });
    }, []);

    const handleError = useCallback((err: unknown, defaultMessage: string) => {
        setFormState(prev => ({
            ...prev,
            updating: false,
            error: getErrorMessage(err, defaultMessage),
        }));
    }, []);

    return {
        formState,
        setEditingResource,
        setUpdating,
        setError,
        resetState,
        handleError,
    };
}