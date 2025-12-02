interface FormLabelProps {
    id: string;
    label: string;
    required?: boolean;
}

export function FormLabel({ id, label, required }: FormLabelProps) {
    return (
        <label htmlFor={id}>
            {label}
            {required && <span className="required">*</span>}
        </label>
    );
}

