import React, { useState, useEffect } from 'react';
import './Toast.css';

interface ToastProps {
    message: string;
    type: 'error' | 'info';
}

const Toast: React.FC<ToastProps> = ({ message, type }) => {
    const [visible, setVisible] = useState(false);

    useEffect(() => {
        if (message) {
            setVisible(true);
            const timer = setTimeout(() => setVisible(false), 3000);
            return () => clearTimeout(timer);
        }
    }, [message]);

    return (
        visible && (
            <div className={`toast toast-${type}`}>
                <p>{message}</p>
            </div>
        )
    );
};

export default Toast;
