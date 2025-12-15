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
    maxLength?: number;
    register?: UseFormRegisterReturn; // react-hook-form register return
    value?: string;
    onChange?: (e: React.ChangeEvent<HTMLTextAreaElement>) => void;
}

export function FormTextarea({
    id,
    label,
    required = false,
    error,
    placeholder,
    rows = 2,
    maxLength,
    register,
    value,
    onChange,
}: FormTextareaProps) {
    const textareaProps = register
        ? { ...register }
        : {
              value: value || '',
              onChange,
          };

    return (
        <div className="form-group">
            <FormLabel id={id} label={label} required={required} />
            <textarea
                id={id}
                {...textareaProps}
                rows={rows}
                placeholder={placeholder}
                maxLength={maxLength}
                aria-invalid={error ? 'true' : 'false'}
                onInvalid={(e) => e.preventDefault()}
            />
            <FieldError error={error} />
        </div>
    );
}

