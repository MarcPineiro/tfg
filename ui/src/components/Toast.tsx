import React, { useState, useEffect } from 'react';
import '../css/Toast.css';

interface ToastProps {
    message: string;
    type: 'error' | 'info';
    setMessge: (state: string) => void;
}

const Toast: React.FC<ToastProps> = ({ message, type, setMessge }) => {
    const [visible, setVisible] = useState(false);

    useEffect(() => {
        if (message) {
            setVisible(true);
            const timer = setTimeout(() => {setVisible(false);setMessge('');}, 3000);
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
