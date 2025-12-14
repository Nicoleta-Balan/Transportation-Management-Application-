import { useState, useEffect, useCallback } from 'react';
import { getErrorMessage } from '../utils/errorUtils';

export interface ResourceAPI<T> {
    getAll: () => Promise<T[]>;
    delete: (id: number) => Promise<void>;
    getAllForRoute?: (routeId: number) => Promise<T[]>;
}

interface UseResourceOptions<T> {
    resourceName: string;
    getDeleteMessage?: (item: T) => string;
    loadOnMount?: boolean;
    routeId?: number | null;
    normalizeData?: (items: T[]) => T[];
}

interface ResourceState {
    loading: boolean;
    error: string | null;
    deleting: number | null;
    deleteError: Record<number, string>;
}

export function useResource<T extends { id: number }>(
    api: ResourceAPI<T>,
    options: UseResourceOptions<T>
) {
    const { resourceName, getDeleteMessage, loadOnMount = true, routeId, normalizeData } = options;

    // Data state
    const [data, setData] = useState<T[]>([]);
    
    // Resource state (loading, errors, etc.)
    const [state, setState] = useState<ResourceState>({
        loading: false,
        error: null,
        deleting: null,
        deleteError: {},
    });

    const load = useCallback(async () => {
        // For route-scoped resources, handle null routeId
        if (routeId !== undefined && routeId === null) {
            setData([]);
            setState(prev => ({ ...prev, loading: false }));
            return;
        }

        setState(prev => ({ ...prev, loading: true, error: null }));
        try {
            let items: T[];
            
            // Use route-specific loading if routeId is provided and API supports it
            if (routeId !== undefined && routeId !== null && api.getAllForRoute) {
                items = await api.getAllForRoute(routeId);
            } else {
                items = await api.getAll();
            }
            
            // Apply normalization if provided
            const normalizedItems = normalizeData ? normalizeData(items) : items;
            setData(normalizedItems);
            setState(prev => ({ ...prev, loading: false }));
        } catch (err) {
            setState(prev => ({
                ...prev,
                loading: false,
                error: getErrorMessage(err, `Failed to load ${resourceName}s`),
            }));
        }
    }, [api, resourceName, routeId, normalizeData]);

    const handleDeleteClick = useCallback((item: T) => {
        // Generate confirmation message
        const message = getDeleteMessage?.(item) 
            || `Are you sure you want to delete this ${resourceName}? This action cannot be undone.`;
        
        if (window.confirm(message)) {
            handleDeleteConfirm(item.id);
        }
    }, [getDeleteMessage, resourceName]);

    const handleDeleteConfirm = useCallback(async (id: number) => {
        // Clear any previous error for this item
        setState(prev => ({
            ...prev,
            deleting: id,
            deleteError: (() => {
                const newErrors = { ...prev.deleteError };
                delete newErrors[id];
                return newErrors;
            })(),
        }));

        try {
            await api.delete(id);
            await load(); // Refresh the list
            
            // Clear error on success
            setState(prev => {
                const newErrors = { ...prev.deleteError };
                delete newErrors[id];
                return {
                    ...prev,
                    deleting: null,
                    deleteError: newErrors,
                };
            });
        } catch (err) {
            const errorMessage = getErrorMessage(err, `Failed to delete ${resourceName}`);
            setState(prev => ({
                ...prev,
                deleting: null,
                deleteError: {
                    ...prev.deleteError,
                    [id]: errorMessage,
                },
            }));
        }
    }, [api, load, resourceName]);

    // Load data on mount or when routeId changes (if enabled)
    useEffect(() => {
        if (loadOnMount) {
            load();
        }
    }, [load, loadOnMount, routeId]);

    return {
        // Data
        data,
        setData,
        
        // State
        loading: state.loading,
        error: state.error,
        
        // Operations
        load,
        loadData: load, // Alias for backward compatibility
        
        // Delete state
        deleting: state.deleting,
        deleteError: state.deleteError,
        
        // Delete handlers
        handleDeleteClick,
        handleDeleteConfirm,
    };
}

