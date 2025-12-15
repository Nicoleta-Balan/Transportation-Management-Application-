import { useState, useCallback, useEffect } from 'react';

import { useRoutes } from '../../hooks/useRoutes';
import { useStations } from '../../hooks/useStations';
import { useCreateRouteForm } from '../../hooks/useCreateRouteForm';
import { useRouteFilters } from '../../hooks/useRouteFilters';

import { RouteEditFormProvider, useRouteEditFormContext } from '../../contexts/RouteEditFormContext';

import { MAP_CONFIG } from '../../constants/stationConstants';

import RouteMap from './map/RouteMap';
import { RouteForm } from './form/RouteForm';
import RoutesTable from './list/RoutesTable';
import { DataListContent } from '../common/DataListContent';
import { ResourcePage } from '../common/ResourcePage';

import type { Route } from '../../types/Route';

import './RoutesPage.css';

interface RoutesListContentProps {
    loading: boolean;
    error: string | null;
    routes: Route[];
    sortedAndFilteredRoutes: ReturnType<typeof useRouteFilters>['sortedAndFilteredRoutes'];
    sortColumn: ReturnType<typeof useRouteFilters>['sortColumn'];
    sortDirection: ReturnType<typeof useRouteFilters>['sortDirection'];
    deleting: number | null;
    deleteError: Record<number, string>;
    searchTerm: string;
    onSearchChange: (value: string) => void;
    onSort: ReturnType<typeof useRouteFilters>['handleSort'];
    onDeleteClick: (route: Route) => void;
    onTimetableClick?: (route: Route) => void;
    viewingTimetableRouteId?: number | null;
}

function RoutesListContent({
    loading,
    error,
    routes,
    sortedAndFilteredRoutes,
    sortColumn,
    sortDirection,
    deleting,
    deleteError,
    searchTerm,
    onSearchChange,
    onSort,
    onDeleteClick,
    onTimetableClick,
    viewingTimetableRouteId,
}: RoutesListContentProps) {
    return (
        <DataListContent
            loading={loading}
            error={error}
            data={routes}
            emptyMessage="No routes found. Create one above!"
        >
            {() => (
                <RoutesTable
                    sortedAndFilteredRoutes={sortedAndFilteredRoutes}
                    sortColumn={sortColumn}
                    sortDirection={sortDirection}
                    deleting={deleting}
                    deleteError={deleteError}
                    searchTerm={searchTerm}
                    onSearchChange={onSearchChange}
                    onSort={onSort}
                    onDeleteClick={onDeleteClick}
                    onTimetableClick={onTimetableClick}
                    viewingTimetableRouteId={viewingTimetableRouteId ?? null}
                />
            )}
        </DataListContent>
    );
}

interface RoutesPageContentProps {
    routesData: ReturnType<typeof useRoutes>;
}

function RoutesPageContent({ routesData }: RoutesPageContentProps) {
    const stationsData = useStations();
    
    const [isCreateFormExpanded, setIsCreateFormExpanded] = useState(false);
    const [viewingTimetableRouteId, setViewingTimetableRouteId] = useState<number | null>(null);

    // Create form hook
    const handleRouteCreated = useCallback((routeId: number) => {
        // Collapse the create form
        setIsCreateFormExpanded(false);
        // Open the timetable for the created route
        setViewingTimetableRouteId(routeId);
        // Scroll to the route entry after a short delay to allow DOM update
        setTimeout(() => {
            const routeElement = document.getElementById(`route-row-${routeId}`);
            if (routeElement) {
                routeElement.scrollIntoView({ behavior: 'smooth', block: 'center' });
            }
        }, 100);
    }, []);

    const createForm = useCreateRouteForm(routesData.loadRoutes, handleRouteCreated);

    // Get edit form from context
    const editForm = useRouteEditFormContext();

    // Collapse all timetable panels when a route enters edit mode
    useEffect(() => {
        if (editForm.editingRoute) {
            setViewingTimetableRouteId(null);
        }
    }, [editForm.editingRoute]);

    // Filters
    const {
        searchTerm,
        setSearchTerm,
        sortColumn,
        sortDirection,
        handleSort,
        sortedAndFilteredRoutes,
    } = useRouteFilters(routesData.routes);

    // Get start and end stations from selected stations
    const startStation = createForm.selectedStations[0] || null;
    const endStation = createForm.selectedStations[createForm.selectedStations.length - 1] || null;

    // Get route stations when viewing timetable
    const viewingTimetableRoute = viewingTimetableRouteId 
        ? routesData.routes.find(r => r.id === viewingTimetableRouteId)
        : null;
    const routeStations = viewingTimetableRoute?.routeStops
        ? viewingTimetableRoute.routeStops
            .sort((a, b) => a.sequenceOrder - b.sequenceOrder)
            .map(stop => stop.station)
        : [];

    // Mutually exclusive panels
    const handleCreateFormToggle = () => {
        if (editForm.editingRoute) {
            editForm.resetForm();
        }
        if (viewingTimetableRouteId !== null) {
            setViewingTimetableRouteId(null);
        }
        setIsCreateFormExpanded(!isCreateFormExpanded);
    };

    const handleTimetableClick = (route: Route) => {
        if (editForm.editingRoute) {
            editForm.resetForm();
        }
        if (isCreateFormExpanded) {
            setIsCreateFormExpanded(false);
        }
        setViewingTimetableRouteId(viewingTimetableRouteId === route.id ? null : route.id);
    };

    return (
        <ResourcePage
            title="Route Management"
            pageClassName="routes-page"
            mapSection={
                <RouteMap
                    stations={stationsData.stations}
                    selectedStations={
                        isCreateFormExpanded 
                            ? createForm.selectedStations 
                            : editForm.editingRoute 
                                ? editForm.selectedStations 
                                : viewingTimetableRouteId
                                    ? routeStations
                                    : []
                    }
                    onStationSelect={
                        isCreateFormExpanded 
                            ? createForm.handleStationSelect 
                            : editForm.editingRoute 
                                ? editForm.handleStationSelect 
                                : () => {} // No-op when no form is active
                    }
                    height={MAP_CONFIG.MAP_HEIGHT}
                />
            }
            createFormSection={{
                title: "Create New Route",
                className: "route-form-section",
                isExpanded: isCreateFormExpanded,
                onToggle: handleCreateFormToggle,
                content: (
                    <RouteForm
                        register={createForm.register}
                        handleSubmit={createForm.handleSubmit}
                        errors={createForm.errors}
                        submitting={createForm.submitting}
                        error={createForm.error}
                        startStation={startStation}
                        endStation={endStation}
                        selectedStations={createForm.selectedStations}
                        calculatedDistance={createForm.calculatedDistance}
                        calculatedDurationMinutes={createForm.calculatedDurationMinutes}
                        segmentOverrides={createForm.segmentOverrides}
                        onSegmentChange={createForm.handleSegmentChange}
                        onSubmit={createForm.onSubmit}
                    />
                ),
            }}
            listSection={
                <RoutesListContent
                    loading={routesData.loading}
                    error={routesData.error}
                    routes={routesData.routes}
                    sortedAndFilteredRoutes={sortedAndFilteredRoutes}
                    sortColumn={sortColumn}
                    sortDirection={sortDirection}
                    deleting={routesData.deleting}
                    deleteError={routesData.deleteError}
                    searchTerm={searchTerm}
                    onSearchChange={setSearchTerm}
                    onSort={handleSort}
                    onDeleteClick={routesData.handleDeleteClick}
                    onTimetableClick={handleTimetableClick}
                    viewingTimetableRouteId={viewingTimetableRouteId}
                />
            }
        />
    );
}

export default function RoutesPage() {
    const routesData = useRoutes();

    return (
        <RouteEditFormProvider loadRoutes={routesData.loadRoutes}>
            <RoutesPageContent routesData={routesData} />
        </RouteEditFormProvider>
    );
}
