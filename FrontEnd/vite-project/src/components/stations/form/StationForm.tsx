import type { UseFormRegister, UseFormHandleSubmit, FieldErrors, UseFormWatch } from 'react-hook-form';

import type { Station, CreateStationRequest, UpdateStationRequest } from '../../../types/Station';

import { CREATE_FORM_FIELDS, EDIT_FORM_FIELDS } from '../../../config/stationFormConfig';

import { FormFieldRenderer, LocationFieldErrors } from './FormFieldRenderer';
import { SubmitButton, CancelButton } from '../../buttons';
import { ErrorAlert } from '../../ErrorAlert';

// Props for create mode
interface CreateFormProps {
    mode: 'create';
    register: UseFormRegister<CreateStationRequest>;
    handleSubmit: UseFormHandleSubmit<CreateStationRequest>;
    errors: FieldErrors<CreateStationRequest>;
    isValid: boolean;
    submitting: boolean;
    formError: string | null;
    watchedAddress: string | undefined;
    shouldShowAddressError: boolean;
    onSubmit: (data: CreateStationRequest) => void;
}

// Props for edit mode
interface EditFormProps {
    mode: 'edit';
    station: Station;
    register: UseFormRegister<UpdateStationRequest>;
    handleSubmit: UseFormHandleSubmit<UpdateStationRequest>;
    watch: UseFormWatch<UpdateStationRequest>;
    errors: FieldErrors<UpdateStationRequest>;
    isValid: boolean;
    updating: boolean;
    editError: string | null;
    onSubmit: (data: UpdateStationRequest) => void;
    onCancel: () => void;
}

type StationFormProps = CreateFormProps | EditFormProps;

export default function StationForm(props: StationFormProps) {
    if (props.mode === 'create') {
        return <CreateForm {...props} />;
    } else {
        return <EditForm {...props} />;
    }
}

// Create form component
function CreateForm({
    register,
    handleSubmit,
    errors,
    isValid,
    submitting,
    formError,
    watchedAddress,
    shouldShowAddressError,
    onSubmit,
}: CreateFormProps) {
    return (
        <form onSubmit={handleSubmit(onSubmit)} className="station-form" noValidate>
            {/* Render fields from configuration */}
            {CREATE_FORM_FIELDS.map((field) => (
                <FormFieldRenderer
                    key={field.key}
                    field={field}
                    register={register}
                    errors={errors}
                    watchedAddress={watchedAddress}
                    shouldShowAddressError={shouldShowAddressError}
                />
            ))}

            {/* Location field errors */}
            <LocationFieldErrors errors={errors} />

            <ErrorAlert error={formError} />

            <SubmitButton
                mode="save"
                isSubmitting={submitting}
                isValid={isValid}
                submittingLabel="Saving..."
                ariaLabel="Save station"
            />
        </form>
    );
}

// Edit form component
function EditForm({
    station,
    register,
    handleSubmit,
    watch,
    errors,
    isValid,
    updating,
    editError,
    onSubmit,
    onCancel,
}: EditFormProps) {
    // Get watched address value for edit mode address field
    const watchedAddress = watch('address');
    
    return (
        <div className="edit-form-container">
            <form onSubmit={handleSubmit(onSubmit)} className="edit-form" noValidate>
                {/* Render fields from configuration */}
                {EDIT_FORM_FIELDS.map((field) => (
                    <FormFieldRenderer
                        key={field.key}
                        field={field}
                        register={register}
                        errors={errors}
                        watchedAddress={field.key === 'address' ? watchedAddress : undefined}
                        stationName={station.name}
                        fieldIdPrefix="edit-"
                    />
                ))}

                {/* Error Display */}
                <ErrorAlert error={editError} />

                {/* Submit and Cancel Buttons */}
                <div className="form-actions">
                    <SubmitButton
                        mode="save"
                        isSubmitting={updating}
                        isValid={isValid}
                        submittingLabel="Saving..."
                        ariaLabel="Save station"
                    />
                    <CancelButton
                        onClick={onCancel}
                        disabled={updating}
                        ariaLabel="Cancel editing"
                    />
                </div>
            </form>
        </div>
    );
}

