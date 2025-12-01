import { StationStatus } from '../types/Station';
import type { RegisterOptions } from 'react-hook-form';
import {
    nameValidation,
    descriptionValidation,
    statusValidation,
    addressValidation,
    latitudeValidation,
    longitudeValidation,
} from '../validation/stationValidationSchemas';

export const STATUS_OPTIONS: Array<{ value: string; label: string }> = [
    { value: StationStatus.ACTIVE, label: 'Active' },
    { value: StationStatus.INACTIVE, label: 'Inactive' },
    { value: StationStatus.MAINTENANCE, label: 'Maintenance' },
];

interface BaseFieldConfig {
    key: string;
    label: string;
    required?: boolean;
    placeholder?: string;
    helpText?: string;
}

interface InputFieldConfig extends BaseFieldConfig {
    type: 'input';
    readOnly?: boolean;
    value?: string;
    ariaDescribedBy?: string;
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    validation: RegisterOptions<any, any>;
}

interface TextareaFieldConfig extends BaseFieldConfig {
    type: 'textarea';
    rows?: number;
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    validation: RegisterOptions<any, any>;
}

interface SelectFieldConfig extends BaseFieldConfig {
    type: 'select';
    options: Array<{ value: string; label: string }>;
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    validation: RegisterOptions<any, any>;
}

interface ReadonlyFieldConfig extends BaseFieldConfig {
    type: 'readonly';
    value: string;
}

interface HiddenFieldConfig {
    type: 'hidden';
    key: string;
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    validation: RegisterOptions<any, any>;
}

export type FieldConfig = 
    | InputFieldConfig 
    | TextareaFieldConfig 
    | SelectFieldConfig 
    | ReadonlyFieldConfig 
    | HiddenFieldConfig;

const BASE_FORM_FIELDS: FieldConfig[] = [
    {
        type: 'hidden',
        key: 'latitude',
        validation: latitudeValidation,
    },
    {
        type: 'hidden',
        key: 'longitude',
        validation: longitudeValidation,
    },
];

const createDescriptionField: TextareaFieldConfig = {
    type: 'textarea',
    key: 'description',
    label: 'Description',
    placeholder: 'Enter station description (optional)',
    rows: 2,
    validation: descriptionValidation,
};

const editDescriptionField: TextareaFieldConfig = {
    ...createDescriptionField,
    rows: 3,
};

const createStatusField: SelectFieldConfig = {
    type: 'select',
    key: 'status',
    label: 'Status',
    required: true,
    placeholder: 'Select status...',
    options: STATUS_OPTIONS,
    validation: statusValidation,
};

const editStatusField: SelectFieldConfig = {
    ...createStatusField,
    placeholder: 'Select status',
};

const createAddressField: InputFieldConfig = {
    type: 'input',
    key: 'address',
    label: 'Address',
    required: true,
    placeholder: 'Address will be auto-filled when you click the map',
    readOnly: true,
    helpText: 'Address is automatically filled when you click on the map',
    ariaDescribedBy: 'address-help',
    validation: addressValidation,
};

const editAddressField: InputFieldConfig = {
    ...createAddressField,
    helpText: 'The address is updated when the red dot on the map is moved',
    ariaDescribedBy: 'edit-address-help',
};

/**
 * Form field configuration for create mode
 * Combines name field with shared fields customized for create mode
 */
export const CREATE_FORM_FIELDS: FieldConfig[] = [
    {
        type: 'input',
        key: 'name',
        label: 'Station Name',
        required: true,
        placeholder: 'Enter station name',
        validation: nameValidation,
    },
    createDescriptionField,
    createStatusField,
    ...BASE_FORM_FIELDS,
    createAddressField,
];

export const EDIT_FORM_FIELDS: FieldConfig[] = [
    {
        type: 'readonly',
        key: 'name',
        label: 'Station Name',
        value: '', // Will be provided dynamically from station prop
        helpText: 'Station name cannot be changed',
    },
    editDescriptionField,
    editStatusField,
    ...BASE_FORM_FIELDS,
    editAddressField,
];

