import { useCallback, useMemo } from 'react';

interface FormSubmitConfig<TRequest, TResponse> {
    submitFn: (request: TRequest) => Promise<TResponse>;
    errorMessage: string;
    transformRequest?: (data: TRequest) => TRequest;
    validate?: (data: TRequest) => string | null;
    onSuccess?: (response: TResponse) => void | Promise<void>;
    onReset?: () => void;
    onReload?: () => void | Promise<void>;
    onError?: (err: unknown, defaultHandler: (err: unknown, message: string) => void) => void;
}

interface UseFormSubmitResult<TRequest> {
    handleSubmit: (data: TRequest) => Promise<void>;
}

export function useFormSubmit<TRequest, TResponse>(
    setLoading: (loading: boolean) => void,
    handleError: (err: unknown, defaultMessage: string) => void,
    config: FormSubmitConfig<TRequest, TResponse>
): UseFormSubmitResult<TRequest> {
    // Memoize config to prevent unnecessary recreations of handleSubmit
    // The config object itself is stable, but its function properties may change
    const {
        submitFn,
        errorMessage,
        transformRequest,
        validate,
        onSuccess,
        onReset,
        onReload,
        onError,
    } = useMemo(() => config, [
        config.submitFn,
        config.errorMessage,
        config.transformRequest,
        config.validate,
        config.onSuccess,
        config.onReset,
        config.onReload,
        config.onError,
    ]);

    const handleSubmit = useCallback(async (data: TRequest) => {
        // Custom validation
        if (validate) {
            const validationError = validate(data);
            if (validationError) {
                handleError(new Error(validationError), 'Validation failed');
                return;
            }
        }

        // Transform request if needed
        const request = transformRequest ? transformRequest(data) : data;

        setLoading(true);

        try {
            const response = await submitFn(request);
            
            // Call success callback
            if (onSuccess) {
                await onSuccess(response);
            }
            
            // Reset form if needed
            if (onReset) {
                onReset();
            }
            
            // Reload data if needed
            if (onReload) {
                await onReload();
            }
        } catch (err) {
            // Custom error handling
            if (onError) {
                onError(err, (error, message) => handleError(error, message));
            } else {
                handleError(err, errorMessage);
            }
        } finally {
            setLoading(false);
        }
    }, [
        validate,
        transformRequest,
        submitFn,
        setLoading,
        handleError,
        errorMessage,
        onSuccess,
        onReset,
        onReload,
        onError,
    ]);

    return { handleSubmit };
}

