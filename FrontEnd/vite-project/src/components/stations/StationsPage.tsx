import { useState, useMemo, useCallback } from 'react';

import { useStations } from '../../hooks/useStations';
import { useCreateStationForm } from '../../hooks/useCreateStationForm';
import { useStationFilters, type StationSortColumn } from '../../hooks/useStationFilters';

import { StationEditFormProvider, useStationEditFormContext } from '../../contexts/StationEditFormContext';

import { MAP_CONFIG } from '../../constants/stationConstants';

import StationMap from './map/StationMap';
import StationForm from './form/StationForm';
import StationsTable from './list/StationsTable';
import { DataListContent } from '../common/DataListContent';
import { ResourcePage } from '../common/ResourcePage';

import type { Station } from '../../types/Station';

import './StationsPage.css';

interface StationsListContentProps {
    loading: boolean;
    error: string | null;
    stations: Station[];
    sortedAndFilteredStations: Station[];
    sortColumn: StationSortColumn | null;
    sortDirection: 'asc' | 'desc';
    deleting: number | null;
    deleteError: Record<number, string>;
    searchTerm: string;
    onSearchChange: (value: string) => void;
    onSort: (column: StationSortColumn) => void;
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
    return (
        <DataListContent
            loading={loading}
            error={error}
            data={stations}
            emptyMessage="No stations found. Create one above!"
        >
            {() => (
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
            )}
        </DataListContent>
    );
}

function StationsPageContent({ stationsData }: { stationsData: ReturnType<typeof useStations> }) {
    const [isCreateFormExpanded, setIsCreateFormExpanded] = useState(false);

    // Handle station created callback
    const handleStationCreated = useCallback((stationId: number) => {
        // Collapse the create form
        setIsCreateFormExpanded(false);
        // Scroll to the station entry after a short delay to allow DOM update
        setTimeout(() => {
            const stationElement = document.getElementById(`station-row-${stationId}`);
            if (stationElement) {
                stationElement.scrollIntoView({ behavior: 'smooth', block: 'center' });
            }
        }, 100);
    }, []);

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
    } = useCreateStationForm(stationsData.setStations, stationsData.stations, handleStationCreated);

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
        <ResourcePage
            title="Station Management"
            pageClassName="stations-page"
            mapSection={
                <StationMap
                    {...mapProps}
                    existingStations={stationsData.stations}
                    height={MAP_CONFIG.MAP_HEIGHT}
                />
            }
            createFormSection={{
                title: "Create New Station",
                className: "station-form-section",
                isExpanded: isCreateFormExpanded,
                onToggle: () => setIsCreateFormExpanded(!isCreateFormExpanded),
                content: (
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
                ),
            }}
            listSection={
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
            }
        />
    );
}

export default function StationsPage() {
    const stationsData = useStations();

    return (
        <StationEditFormProvider loadStations={stationsData.loadStations}>
            <StationsPageContent stationsData={stationsData} />
        </StationEditFormProvider>
    );
}