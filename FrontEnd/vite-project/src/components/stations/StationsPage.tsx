import { useState, useMemo } from 'react';

import { useStations } from '../../hooks/useStations';
import { useCreateStationForm } from '../../hooks/useCreateStationForm';
import { useStationFilters } from '../../hooks/useStationFilters';

import { StationEditFormProvider, useStationEditFormContext } from '../../contexts/StationEditFormContext';

import { MAP_CONFIG } from '../../constants/stationConstants';

import StationMap from './map/StationMap';
import StationForm from './form/StationForm';
import StationsTable from './list/StationsTable';
import { ErrorAlert } from '../ErrorAlert';
import { ErrorBoundary } from '../ErrorBoundary';

import type { Station } from '../../types/Station';

import './StationsPage.css';

interface StationsListContentProps {
    loading: boolean;
    error: string | null;
    stations: Station[];
    sortedAndFilteredStations: Station[];
    sortColumn: keyof Station | null;
    sortDirection: 'asc' | 'desc';
    deleting: number | null;
    deleteError: Record<number, string>;
    searchTerm: string;
    onSearchChange: (value: string) => void;
    onSort: (column: keyof Station) => void;
    onDeleteClick: (station: Station) => void;
}

function StationsListContent({
    loading,
    error,
    stations,
    sortedAndFilteredStations,
    sortColumn,
    sortDirection,
    deleting,
    deleteError,
    searchTerm,
    onSearchChange,
    onSort,
    onDeleteClick,
}: StationsListContentProps) {
    if (loading && stations.length === 0) {
        return <div className="loading">Loading stations...</div>;
    }

    if (error && stations.length === 0) {
        return <ErrorAlert error={error} />;
    }

    if (stations.length === 0) {
        return <div className="empty-state">No stations found. Create one above!</div>;
    }

    return (
        <StationsTable
            sortedAndFilteredStations={sortedAndFilteredStations}
            sortColumn={sortColumn}
            sortDirection={sortDirection}
            deleting={deleting}
            deleteError={deleteError}
            searchTerm={searchTerm}
            onSearchChange={onSearchChange}
            onSort={onSort}
            onDeleteClick={onDeleteClick}
        />
    );
}

function StationsPageContent({ stationsData }: { stationsData: ReturnType<typeof useStations> }) {
    const [isCreateFormExpanded, setIsCreateFormExpanded] = useState(false);

    // Create form hook
    const {
        register,
        handleSubmit,
        errors,
        isValid,
        submitting,
        error: formError,
        selectedLocation,
        handleMapLocationSelect,
        onSubmit,
        watchedAddress,
        shouldShowAddressError,
    } = useCreateStationForm(stationsData.setStations, stationsData.stations);

    // Edit form context (replaces useEditStationForm hook)
    const {
        editingStation,
        editSelectedLocation,
        handleEditMapLocationSelect,
    } = useStationEditFormContext();

    const {
        searchTerm,
        setSearchTerm,
        sortColumn,
        sortDirection,
        handleSort,
        sortedAndFilteredStations,
    } = useStationFilters(stationsData.stations);

    /**
     * Computed map props based on edit/create mode
     * Memoized to prevent unnecessary recalculations and improve performance
     * Both green (create) and red (edit) markers can be visible simultaneously
     * - Green marker: controls create form address (createFormSelectedLocation)
     * - Red marker: controls edit form address (editSelectedLocation)
     */
    const mapProps = useMemo(() => ({
        key: editingStation ? `edit-map-${editingStation.id}` : 'create-map',
        onLocationSelect: editingStation ? handleEditMapLocationSelect : handleMapLocationSelect,
        selectedLocation: editingStation ? editSelectedLocation : null, // Red marker location (edit mode only)
        initialLocation: editingStation 
            ? { lat: editingStation.latitude, lng: editingStation.longitude } 
            : undefined,
        editingStationId: editingStation?.id || null,
        onCreateFormLocationSelect: handleMapLocationSelect,
        isCreateFormExpanded,
        createFormSelectedLocation: selectedLocation, // Green marker location (create form)
    }), [editingStation, handleEditMapLocationSelect, handleMapLocationSelect, editSelectedLocation, selectedLocation, isCreateFormExpanded]);

    return (
        <div className="stations-page">
            <h1>Station Management</h1>

            {/* Map Section */}
            <section className="stations-map-section">
                <ErrorBoundary sectionName="Station Map">
                    <div className="stations-map-container">
                        <StationMap
                            {...mapProps}
                            existingStations={stationsData.stations}
                            height={MAP_CONFIG.MAP_HEIGHT}
                        />
                    </div>
                </ErrorBoundary>
            </section>

            {/* Create Station Form */}
            <section className="station-form-section">
                <div className="form-header-toggle" onClick={() => setIsCreateFormExpanded(!isCreateFormExpanded)}>
                    <h2>Create New Station</h2>
                    <button 
                        type="button" 
                        className="collapse-toggle"
                        aria-label={isCreateFormExpanded ? 'Collapse form' : 'Expand form'}
                        aria-expanded={isCreateFormExpanded}
                    >
                        {isCreateFormExpanded ? '−' : '+'}
                    </button>
                </div>
                {isCreateFormExpanded && (
                    <ErrorBoundary sectionName="Create Station Form">
                        <StationForm
                            mode="create"
                            register={register}
                            handleSubmit={handleSubmit}
                            errors={errors}
                            isValid={isValid}
                            submitting={submitting}
                            formError={formError}
                            watchedAddress={watchedAddress}
                            shouldShowAddressError={shouldShowAddressError}
                            onSubmit={onSubmit}
                        />
                    </ErrorBoundary>
                )}
            </section>

            {/* Stations List */}
            <section className="stations-list-section">
                <ErrorBoundary sectionName="Stations Table">
                    <StationsListContent
                        loading={stationsData.loading}
                        error={stationsData.error}
                        stations={stationsData.stations}
                        sortedAndFilteredStations={sortedAndFilteredStations}
                        sortColumn={sortColumn}
                        sortDirection={sortDirection}
                        deleting={stationsData.deleting}
                        deleteError={stationsData.deleteError}
                        searchTerm={searchTerm}
                        onSearchChange={setSearchTerm}
                        onSort={handleSort}
                        onDeleteClick={stationsData.handleDeleteClick}
                    />
                </ErrorBoundary>
            </section>
        </div>
    );
}

/**
 * Main stations page component
 * Provides edit form context and renders page content
 */
export default function StationsPage() {
    const stationsData = useStations();

    return (
        <StationEditFormProvider loadStations={stationsData.loadStations}>
            <StationsPageContent stationsData={stationsData} />
        </StationEditFormProvider>
    );
}