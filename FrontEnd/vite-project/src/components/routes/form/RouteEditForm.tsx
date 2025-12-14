import { useState } from 'react';
import type { UseFormRegister, UseFormHandleSubmit, FieldErrors } from 'react-hook-form';
import type { UpdateRouteRequest, CreateRouteRequest } from '../../../types/Route';
import type { Station } from '../../../types/Station';

import { SubmitButton, CancelButton } from '../../buttons';
import { ErrorAlert } from '../../ErrorAlert';
import { RouteFormFields } from './RouteFormFields';
import { routeApi } from '../../../services/routeApi';
import { getErrorMessage } from '../../../utils/errorUtils';
import { useRouteEditFormContext } from '../../../contexts/RouteEditFormContext';
import { buildRouteStops } from '../../../utils/routeStopBuilder';
import { validateRouteData, normalizeDescription } from '../../../utils/routeValidation';

interface RouteEditFormProps {
    register: UseFormRegister<UpdateRouteRequest>;
    handleSubmit: UseFormHandleSubmit<UpdateRouteRequest>;
    errors: FieldErrors<UpdateRouteRequest>;
    updating: boolean;
    error: string | null;
    startStation: Station | null;
    endStation: Station | null;
    selectedStations?: Station[];
    calculatedDistance: number;
    calculatedDurationMinutes: number;
    segmentOverrides: Map<number, { distance: number; duration: number }>;
    onSegmentChange: (stationIndex: number, distance: number, duration: number) => void;
    onSubmit: (data: UpdateRouteRequest) => void;
    onCancel: () => void;
}

export function RouteEditForm({
    register,
    handleSubmit,
    errors,
    updating,
    error,
    startStation,
    endStation,
    selectedStations = [],
    calculatedDistance,
    calculatedDurationMinutes,
    segmentOverrides,
    onSegmentChange,
    onSubmit,
    onCancel,
}: RouteEditFormProps) {
    const { loadRoutes } = useRouteEditFormContext();
    const [saveAsNew, setSaveAsNew] = useState(false);
    const [creating, setCreating] = useState(false);
    const [createError, setCreateError] = useState<string | null>(null);

    // Wrapper onSubmit that handles both update and create
    const handleFormSubmit = handleSubmit(async (data: UpdateRouteRequest) => {
        if (saveAsNew) {
            // Validate route data
            const validation = validateRouteData(selectedStations, data.distance, data.durationMinutes);
            if (!validation.isValid) {
                setCreateError(validation.error || 'Validation failed');
                return;
            }

            // Build stops array from selected stations and overrides
            const stops = buildRouteStops(selectedStations, segmentOverrides);

            // Create new route
            setCreating(true);
            setCreateError(null);
            try {
                const createData: CreateRouteRequest = {
                    vehicleClass: data.vehicleClass,
                    distance: data.distance,
                    durationMinutes: data.durationMinutes,
                    description: normalizeDescription(data.description),
                    stops,
                };
                await routeApi.createRoute(createData);
                await loadRoutes();
                onCancel();
            } catch (err) {
                setCreateError(getErrorMessage(err, 'Failed to create new route'));
            } finally {
                setCreating(false);
            }
        } else {
            // Normal update flow
            await onSubmit(data);
        }
    });

    const isSubmitting = updating || creating;
    const displayError = error || createError;

    return (
        <form onSubmit={handleFormSubmit} className="route-form" noValidate>
            <RouteFormFields
                register={register}
                errors={errors}
                startStation={startStation}
                endStation={endStation}
                selectedStations={selectedStations}
                calculatedDistance={calculatedDistance}
                calculatedDurationMinutes={calculatedDurationMinutes}
                segmentOverrides={segmentOverrides}
                onSegmentChange={onSegmentChange}
                editable={true}
                idPrefix="edit"
            />

            <ErrorAlert error={displayError} />

            <div className="save-as-new-checkbox">
                <label>
                    <input
                        type="checkbox"
                        checked={saveAsNew}
                        onChange={(e) => setSaveAsNew(e.target.checked)}
                        disabled={isSubmitting}
                    />
                    <span>Save as a New Route</span>
                </label>
            </div>

            <div className="form-actions">
                <SubmitButton
                    mode="save"
                    isSubmitting={isSubmitting}
                    isValid={Boolean(startStation && endStation)}
                    submittingLabel={saveAsNew ? "Creating..." : "Saving..."}
                    ariaLabel={saveAsNew ? "Create new route" : "Save route"}
                />
                <CancelButton onClick={onCancel} ariaLabel="Cancel editing route" />
            </div>
        </form>
    );
}

