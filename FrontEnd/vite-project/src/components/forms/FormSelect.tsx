import type { FieldError as ReactHookFormFieldError } from 'react-hook-form';
import { FieldError } from './FieldError';
import { FormLabel } from './FormLabel';

interface SelectOption {
    value: string;
    label: string;
}

interface FormSelectProps {
    id: string;
    label: string;
    required?: boolean;
    error?: ReactHookFormFieldError;
    options: SelectOption[];
    placeholder?: string;
    register?: ReturnType<any>; // react-hook-form register return
}

export function FormSelect({
    id,
    label,
    required = false,
    error,
    options,
    placeholder = 'Select...',
    register,
}: FormSelectProps) {
    return (
        <div className="form-group">
            <FormLabel id={id} label={label} required={required} />
            <select
                id={id}
                {...(register || {})}
                aria-invalid={error ? 'true' : 'false'}
                onInvalid={(e) => e.preventDefault()}
            >
                {placeholder && <option value="">{placeholder}</option>}
                {options.map((option) => (
                    <option key={option.value} value={option.value}>
                        {option.label}
                    </option>
                ))}
            </select>
            <FieldError error={error} />
        </div>
    );
}

