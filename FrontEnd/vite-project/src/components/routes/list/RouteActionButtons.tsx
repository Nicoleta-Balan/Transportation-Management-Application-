import type { Route } from '../../../types/Route';
import { useRouteEditFormContext } from '../../../contexts/RouteEditFormContext';
import { ResourceActionButtons } from '../../common/ResourceActionButtons';

interface RouteActionButtonsProps {
    route: Route;
    deleting: number | null;
    deleteError: Record<number, string>;
    onDeleteClick: (route: Route) => void;
}

export default function RouteActionButtons({
    route,
    deleting,
    deleteError,
    onDeleteClick,
}: RouteActionButtonsProps) {
    // Get edit form state from context
    const { editingRoute, handleEditClick } = useRouteEditFormContext();
    
    // Helper to get route display name
    const getRouteDisplayName = (r: Route) => {
        if (r.routeStops && r.routeStops.length > 0) {
            const stops = r.routeStops;
            const originName = stops[0]?.station?.name || 'Unknown';
            const destName = stops[stops.length - 1]?.station?.name || 'Unknown';
            return { origin: originName, destination: destName };
        }
        return {
            origin: r.originStation?.name || 'Unknown',
            destination: r.destinationStation?.name || 'Unknown',
        };
    };
    
    const handleActionClick = (e: React.MouseEvent) => {
        e.stopPropagation(); // Prevent row click from firing
    };

    return (
        <div className="action-buttons-container" onClick={handleActionClick}>
            <div className="action-buttons">
                <ResourceActionButtons
                    resource={route}
                    editingId={editingRoute?.id ?? null}
                    deleting={deleting}
                    deleteError={deleteError}
                    onEditClick={handleEditClick}
                    onDeleteClick={onDeleteClick}
                    getEditAriaLabel={(r) => {
                        const name = getRouteDisplayName(r);
                        return `Edit route from ${name.origin} to ${name.destination}`;
                    }}
                    getDeleteAriaLabel={(r) => {
                        const name = getRouteDisplayName(r);
                        return `Delete route from ${name.origin} to ${name.destination}`;
                    }}
                />
            </div>
        </div>
    );
}

