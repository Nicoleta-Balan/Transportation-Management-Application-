import type { FieldError as ReactHookFormFieldError, UseFormRegisterReturn } from 'react-hook-form';
import { FieldError } from './FieldError';
import { FormLabel } from './FormLabel';

interface FormInputProps {
    id: string;
    label: string;
    required?: boolean;
    error?: ReactHookFormFieldError;
    placeholder?: string;
    value?: string;
    readOnly?: boolean;
    helpText?: string;
    ariaDescribedBy?: string;
    register?: UseFormRegisterReturn; // react-hook-form register return
    className?: string;
    type?: string;
}

export function FormInput({
    id,
    label,
    required = false,
    error,
    placeholder,
    value,
    readOnly = false,
    helpText,
    ariaDescribedBy,
    register,
    className,
    type = 'text',
}: FormInputProps) {
    const inputProps = register
        ? { ...register, type }
        : {
              value: value || '',
              readOnly,
              type,
          };

    return (
        <div className="form-group">
            <FormLabel id={id} label={label} required={required} />
            <input
                id={id}
                {...inputProps}
                placeholder={placeholder}
                readOnly={readOnly}
                className={className}
                aria-invalid={error ? 'true' : 'false'}
                aria-describedby={ariaDescribedBy || (helpText ? `${id}-help` : undefined)}
                onInvalid={(e) => e.preventDefault()}
            />
            <FieldError error={error} />
            {helpText && (
                <small id={ariaDescribedBy || `${id}-help`} className="address-help">
                    {helpText}
                </small>
            )}
        </div>
    );
}

