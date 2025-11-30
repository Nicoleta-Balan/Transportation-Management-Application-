import type { ButtonHTMLAttributes, ReactNode } from 'react';

export type ButtonVariant = 'save' | 'update' | 'cancel' | 'edit' | 'delete';

const SUBMIT_VARIANTS: ButtonVariant[] = ['save', 'update'];

interface ButtonBaseProps extends Omit<ButtonHTMLAttributes<HTMLButtonElement>, 'type' | 'className'> {
    variant: ButtonVariant;
    children: ReactNode;
    className?: string;
}

export function ButtonBase({
    variant,
    children,
    className,
    disabled,
    ...props
}: ButtonBaseProps) {
    const baseClassName = `button-base button-${variant}`;
    const finalClassName = className ? `${baseClassName} ${className}` : baseClassName;
    const buttonType = SUBMIT_VARIANTS.includes(variant) ? 'submit' : 'button';

    return (
        <button
            type={buttonType}
            className={finalClassName}
            disabled={disabled}
            {...props}
        >
            {children}
        </button>
    );
}

