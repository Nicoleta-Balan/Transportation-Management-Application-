import type { FieldError } from 'react-hook-form';

interface FieldErrorProps {
    error?: FieldError;
}

export function FieldError({ error }: FieldErrorProps) {
    if (!error) return null;

    return (
        <span className="error-text" role="alert">
            {error.message}
        </span>
    );
}

