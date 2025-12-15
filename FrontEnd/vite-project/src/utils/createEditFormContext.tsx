import { createContext, useContext, type ReactNode } from 'react';

export function createEditFormContext<TContextValue>(
    contextName: string
) {
    const Context = createContext<TContextValue | null>(null);

    interface ProviderProps {
        children: ReactNode;
        value: TContextValue;
    }

    function Provider({ children, value }: ProviderProps) {
        return (
            <Context.Provider value={value}>
                {children}
            </Context.Provider>
        );
    }

    function useContextHook(): TContextValue {
        const context = useContext(Context);
        if (!context) {
            throw new Error(`use${contextName}Context must be used within ${contextName}Provider`);
        }
        return context;
    }

    return {
        Context,
        Provider,
        useContextHook,
    };
}

