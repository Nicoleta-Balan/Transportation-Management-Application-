import type { UseFormRegister, UseFormHandleSubmit, FieldErrors } from 'react-hook-form';
import type { CreateRouteRequest } from '../../../types/Route';
import type { Station } from '../../../types/Station';

import { SubmitButton } from '../../buttons';
import { ErrorAlert } from '../../ErrorAlert';
import { RouteFormFields } from './RouteFormFields';

interface RouteFormProps {
    register: UseFormRegister<CreateRouteRequest>;
    handleSubmit: UseFormHandleSubmit<CreateRouteRequest>;
    errors: FieldErrors<CreateRouteRequest>;
    submitting: boolean;
    error: string | null;
    startStation: Station | null;
    endStation: Station | null;
    selectedStations: Station[];
    calculatedDistance: number;
    calculatedDurationMinutes: number;
    segmentOverrides: Map<number, { distance: number; duration: number }>;
    onSegmentChange: (stationIndex: number, distance: number, duration: number) => void;
    onSubmit: (data: CreateRouteRequest) => void;
}

export function RouteForm({
    register,
    handleSubmit,
    errors,
    submitting,
    error,
    startStation,
    endStation,
    selectedStations,
    calculatedDistance,
    calculatedDurationMinutes,
    segmentOverrides,
    onSegmentChange,
    onSubmit,
}: RouteFormProps) {
    return (
        <form onSubmit={handleSubmit(onSubmit)} className="route-form" noValidate>
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
            />

            <ErrorAlert error={error} />

            <SubmitButton
                mode="save"
                isSubmitting={submitting}
                isValid={Boolean(startStation && endStation)}
                submittingLabel="Saving..."
                ariaLabel="Save route"
            />
        </form>
    );
}

