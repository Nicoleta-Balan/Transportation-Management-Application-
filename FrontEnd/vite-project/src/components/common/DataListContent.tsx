import React from 'react';
import { ErrorAlert } from '../ErrorAlert';

interface DataListContentProps<T> {
    // Loading state - shows loading message when true and data is empty
    loading: boolean;
    
    // Error message - shows error alert when present and data is empty
    error: string | null;
    
    // Array of data items
    data: T[];
    
    // Message to display when data array is empty
    emptyMessage: string;
    
    // Render function that receives the data and returns JSX
    children: (data: T[]) => React.ReactNode;
}

export function DataListContent<T>({
    loading,
    error,
    data,
    emptyMessage,
    children
}: DataListContentProps<T>): React.ReactElement {
    // Priority 1: Show loading state if loading and no data
    if (loading && data.length === 0) {
        return <div className="loading">Loading...</div>;
    }

    // Priority 2: Show error state if error and no data
    if (error && data.length === 0) {
        return <ErrorAlert error={error} />;
    }

    // Priority 3: Show empty state if no data
    if (data.length === 0) {
        return <div className="empty-state">{emptyMessage}</div>;
    }

    // Priority 4: Render content with data
    return <>{children(data)}</>;
}

