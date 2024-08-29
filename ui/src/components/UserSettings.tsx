import React, { useState, useEffect } from 'react';
import { getUserInfoAPI, updateUserInfoAPI, changePasswordAPI } from '../utils/api';
import './UserSettings.css';
import { UserInfo } from '../public/types';
import { useToast } from './ToastProvider';

interface UserSettingsProps {
    onClose: () => void;
}

const UserSettings: React.FC<UserSettingsProps> = ({ onClose }) => {
    const [userInfo, setUserInfo] = useState<UserInfo | null>(null);
    const [editing, setEditing] = useState(false);
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const fetchUserInfo = async () => {
            try {
                const info = await getUserInfoAPI(useToast);
                setUserInfo(info);
            } catch (err) {
                setError('Failed to load user information.');
            }
        };

        fetchUserInfo();
    }, []);

    const handleSave = async () => {
        try {
            await updateUserInfoAPI(userInfo!, useToast);
            if (password && password === confirmPassword) {
                await changePasswordAPI(password, useToast);
            } else if (password) {
                setError('Passwords do not match.');
                return;
            }
            setEditing(false);
        } catch (err) {
            setError('Failed to update information.');
        }
    };

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                <h3>User Settings</h3>
                {error && <p className="error">{error}</p>}
                {userInfo && (
                    <div>
                        <input
                            type="text"
                            value={userInfo.email}
                            onChange={(e) => setUserInfo({ ...userInfo, email: e.target.value })}
                            disabled={!editing}
                        />
                        <input
                            type="text"
                            value={userInfo.firstName}
                            onChange={(e) => setUserInfo({ ...userInfo, firstName: e.target.value })}
                            disabled={!editing}
                        />
                        <input
                            type="text"
                            value={userInfo.lastName}
                            onChange={(e) => setUserInfo({ ...userInfo, lastName: e.target.value })}
                            disabled={!editing}
                        />
                        {editing && (
                            <>
                                <input
                                    type="password"
                                    value={password}
                                    onChange={(e) => setPassword(e.target.value)}
                                    placeholder="New Password"
                                />
                                <input
                                    type="password"
                                    value={confirmPassword}
                                    onChange={(e) => setConfirmPassword(e.target.value)}
                                    placeholder="Confirm Password"
                                />
                            </>
                        )}
                        <button onClick={() => setEditing(!editing)}>{editing ? 'Cancel' : 'Edit'}</button>
                        {editing && <button onClick={handleSave}>Save</button>}
                    </div>
                )}
            </div>
        </div>
    );
};

export default UserSettings;
