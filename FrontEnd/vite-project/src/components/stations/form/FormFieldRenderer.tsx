import type { UseFormRegister, FieldErrors, FieldError, Path, RegisterOptions } from 'react-hook-form';

import type { CreateStationRequest, UpdateStationRequest } from '../../../types/Station';
import type { FieldConfig } from '../../../config/stationFormConfig';

import { FormInput, FormTextarea, FormSelect, FormReadonlyField } from '../../forms';

import { getFieldError } from '../../../utils/typeUtils';

interface FormFieldRendererProps<T extends CreateStationRequest | UpdateStationRequest> {
    field: FieldConfig;
    register: UseFormRegister<T>;
    errors: FieldErrors<T>;
    // Special handling for create form address field
    watchedAddress?: string;
    shouldShowAddressError?: boolean;
    // For edit form readonly name field
    stationName?: string;
    // Field prefix for edit mode (e.g., "edit-")
    fieldIdPrefix?: string;
}

function typeSafeRegister<T extends CreateStationRequest | UpdateStationRequest>(
    register: UseFormRegister<T>,
    fieldKey: string,
    validation: RegisterOptions<any, any>
) {
    return register(fieldKey as Path<T>, validation as RegisterOptions<T, Path<T>>);
}

export function FormFieldRenderer<T extends CreateStationRequest | UpdateStationRequest>({
    field,
    register,
    errors,
    watchedAddress,
    shouldShowAddressError,
    stationName,
    fieldIdPrefix = '',
}: FormFieldRendererProps<T>) {
    const fieldId = `${fieldIdPrefix}${field.key}`;
    // Type-safe error access using utility function
    const error = getFieldError(errors, field.key) as FieldError | undefined;

    // Handle hidden fields
    if (field.type === 'hidden') {
        // Use type-safe register helper instead of 'as any'
        const registerProps = typeSafeRegister(register, field.key, field.validation);
        return (
            <input
                key={field.key}
                type="text"
                {...registerProps}
                style={{ display: 'none' }}
                onInvalid={(e) => e.preventDefault()}
            />
        );
    }

    // Handle readonly fields (edit mode only)
    if (field.type === 'readonly') {
        return (
            <FormReadonlyField
                key={field.key}
                id={fieldId}
                label={field.label}
                value={stationName || field.value || ''}
                helpText={field.helpText}
            />
        );
    }

    // Handle input fields
    if (field.type === 'input') {
        // Special handling for address field in create mode
        const addressError = field.key === 'address' && shouldShowAddressError !== undefined
            ? (shouldShowAddressError ? (error || { message: 'Address is required. Please select a location on the map.', type: 'required' }) : undefined)
            : error;

        // Use type-safe register helper instead of 'as any'
        const registerProps = typeSafeRegister(register, field.key, field.validation);

        return (
            <FormInput
                key={field.key}
                id={fieldId}
                label={field.label}
                required={field.required}
                error={addressError}
                placeholder={field.placeholder}
                value={field.key === 'address' && watchedAddress !== undefined ? watchedAddress : field.value}
                readOnly={field.readOnly}
                helpText={field.helpText}
                ariaDescribedBy={field.ariaDescribedBy}
                register={registerProps}
            />
        );
    }

    // Handle textarea fields
    if (field.type === 'textarea') {
        // Use type-safe register helper instead of 'as any'
        const registerProps = typeSafeRegister(register, field.key, field.validation);

        return (
            <FormTextarea
                key={field.key}
                id={fieldId}
                label={field.label}
                required={field.required}
                error={error}
                placeholder={field.placeholder}
                rows={field.rows}
                register={registerProps}
            />
        );
    }

    // Handle select fields
    if (field.type === 'select') {
        // Use type-safe register helper instead of 'as any'
        const registerProps = typeSafeRegister(register, field.key, field.validation);

        return (
            <FormSelect
                key={field.key}
                id={fieldId}
                label={field.label}
                required={field.required}
                error={error}
                options={field.options}
                placeholder={field.placeholder}
                register={registerProps}
            />
        );
    }

    return null;
}

/**
 * Renders hidden location field errors (latitude/longitude)
 */
export function LocationFieldErrors<T extends CreateStationRequest | UpdateStationRequest>({
    errors,
}: {
    errors: FieldErrors<T>;
}) {
    // Type-safe error access using utility function
    // Cast to FieldError for message access (safe because we know these fields exist)
    const latitudeError = getFieldError(errors, 'latitude') as FieldError | undefined;
    const longitudeError = getFieldError(errors, 'longitude') as FieldError | undefined;

    if (!latitudeError && !longitudeError) {
        return null;
    }

    return (
        <div className="form-group">
            <span className="error-text" role="alert">
                {latitudeError?.message || longitudeError?.message || 'Please select a location on the map'}
            </span>
        </div>
    );
}

