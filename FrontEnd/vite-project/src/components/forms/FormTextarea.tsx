import type { FieldError as ReactHookFormFieldError, UseFormRegisterReturn } from 'react-hook-form';
import { FieldError } from './FieldError';
import { FormLabel } from './FormLabel';

interface FormTextareaProps {
    id: string;
    label: string;
    required?: boolean;
    error?: ReactHookFormFieldError;
    placeholder?: string;
    rows?: number;
    register?: UseFormRegisterReturn; // react-hook-form register return
}

export function FormTextarea({
    id,
    label,
    required = false,
    error,
    placeholder,
    rows = 2,
    register,
}: FormTextareaProps) {
    return (
        <div className="form-group">
            <FormLabel id={id} label={label} required={required} />
            <textarea
                id={id}
                {...(register || {})}
                rows={rows}
                placeholder={placeholder}
                aria-invalid={error ? 'true' : 'false'}
                onInvalid={(e) => e.preventDefault()}
            />
            <FieldError error={error} />
        </div>
    );
}

