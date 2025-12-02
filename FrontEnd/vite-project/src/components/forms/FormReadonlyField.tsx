import { FormLabel } from './FormLabel';

interface FormReadonlyFieldProps {
    id: string;
    label: string;
    value: string;
    helpText?: string;
    className?: string;
}

export function FormReadonlyField({
    id,
    label,
    value,
    helpText,
    className = 'readonly-field',
}: FormReadonlyFieldProps) {
    return (
        <div className="form-group">
            <FormLabel id={id} label={label} />
            <input
                type="text"
                id={id}
                value={value}
                readOnly
                disabled
                className={className}
            />
            {helpText && <small>{helpText}</small>}
        </div>
    );
}

