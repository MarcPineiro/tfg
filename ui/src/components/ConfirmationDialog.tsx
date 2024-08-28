import React from 'react';
import '../css/ConfirmationDialog.css';

interface ConfirmationDialogProps {
    message: string;
    onConfirm: () => void;
    onCancel: () => void;
}

const ConfirmationDialog: React.FC<ConfirmationDialogProps> = ({ message, onConfirm, onCancel }) => {
    return (
        <div className="confirmation-dialog">
            <div className="confirmation-dialog-content">
                <p>{message}</p>
                <div className="confirmation-dialog-actions">
                    <button className="btn-confirm" onClick={onConfirm}>Confirm</button>
                    <button className="btn-cancel" onClick={onCancel}>Cancel</button>
                </div>
            </div>
        </div>
    );
};

export default ConfirmationDialog;
