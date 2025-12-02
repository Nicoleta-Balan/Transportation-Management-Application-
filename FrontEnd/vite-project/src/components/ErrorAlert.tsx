interface ErrorAlertProps {
    error: string | null;
    className?: string;
}

export function ErrorAlert({ error, className = 'error-message' }: ErrorAlertProps) {
    if (!error) return null;

    return <div className={className}>{error}</div>;
}

