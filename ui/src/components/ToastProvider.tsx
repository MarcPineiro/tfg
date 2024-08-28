import React, { createContext, useContext, useState } from 'react';
import Toast from './Toast';

const ToastContext = createContext<any>(null);

export const useToast = () => useContext(ToastContext);

export const ToastProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const [message, setMessage] = useState('');
    const [type, setType] = useState<'error' | 'info'>('info');

    const showToast = (msg: string, toastType: 'error' | 'info' = 'info') => {
        setMessage(msg);
        setType(toastType);
    };

    return (
        <ToastContext.Provider value={showToast}>
            {children}
            <Toast message={message} type={type} />
        </ToastContext.Provider>
    );
};
