import React, { useState, useEffect } from 'react';
import { checkUsernameAPI, shareItemAPI } from '../utils/api';
import './ShareModal.css';
import { Item } from '../public/types';

const ShareModal: React.FC<({
    item: Item;
    onClose: () => void;
    onSuccess: () => void;
})> = ({ item, onClose, onSuccess }) => {
    const [username, setUsername] = useState('');
    const [accessLevel, setAccessLevel] = useState(0);
    const [error, setError] = useState(null);
    const [isCurrentUser, setIsCurrentUser] = useState(false);
    const [isUserValid, setIsUserValid] = useState<boolean | null>(null);

    useEffect(() => {
        const validateUsername = async () => {
            if (username) {
                try {
                    const user = await checkUsernameAPI(username);
                    if (user.id === item.ownerId) {
                        //setError('You cannot share with yourself.');
                        setIsUserValid(false);
                    } else {
                        setError(null);
                        setIsUserValid(true);
                    }
                } catch (error) {
                    //setError('Username does not exist.');
                    setIsUserValid(false);
                }
            }
        };
        validateUsername();
    }, [username]);

    const handleShare = async () => {
        if (isCurrentUser || error) return;
        try {
            await shareItemAPI({ elementId:item.id, shareUsername: username, accessLevel });
            onClose();
        } catch (err) {
            //setError('Failed to share the item.');
        }
    };

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                <h3>Share Item</h3>
                <input
                    type="text"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    placeholder="Enter username"
                />
                <select value={accessLevel} onChange={(e) => {if(e.target.value == "read") setAccessLevel(1); else setAccessLevel(2)}}>
                    <option value="read">Read</option>
                    <option value="write">Write</option>
                </select>
                {error && <p className="error">{error}</p>}
                <button onClick={handleShare} disabled={!!error || isCurrentUser}>
                    Share
                </button>
                <button onClick={onClose}>Cancel</button>
            </div>
        </div>
    );
};

export default ShareModal;
