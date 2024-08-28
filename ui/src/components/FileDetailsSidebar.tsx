import React, { useState, useEffect } from 'react';
import { useToast } from './ToastProvider';
import { getFileDetailsAPI, renameItemAPI, deleteItemAPI } from '../utils/api';
import './FileDetailsSidebar.css';
import { FileInfo } from '../public/types';

const FileDetailsSidebar: React.FC<{ item: any; refreshItems: () => void }> = ({ item, refreshItems }) => {
    const [details, setDetails] = useState<FileInfo | null>(null);
    const [isEditing, setIsEditing] = useState(false);
    const [newName, setNewName] = useState(item.name);
    const [showSave, setShowSave] = useState(false);
    const showToast = useToast();

    useEffect(() => {
        const fetchDetails = async () => {
            try {
                const details = await getFileDetailsAPI(item.id);
                setDetails(details);
            } catch (error) {
                showToast('Failed to load file details.', 'error');
            }
        };
        fetchDetails();
    }, [item]);

    const handleRename = async () => {
        try {
            await renameItemAPI(item.id, newName);
            refreshItems();
            setIsEditing(false);
            setShowSave(false);
            showToast('File renamed successfully!', 'info');
        } catch (error) {
            showToast('Failed to rename file.', 'error');
        }
    };

    const handleDelete = async () => {
        try {
            await deleteItemAPI(item.id);
            refreshItems();
            showToast('File deleted successfully!', 'info');
        } catch (error) {
            showToast('Failed to delete file.', 'error');
        }
    };

    return (
        <div className="file-details-sidebar">
            <h2>{isEditing ? 'Edit Details' : 'File Details'}</h2>
            {isEditing ? (
                <>
                    <input value={newName} onChange={(e) => { setNewName(e.target.value); setShowSave(true); }} />
                    <button onClick={handleRename}>Save</button>
                    <button onClick={() => setIsEditing(false)}>Cancel</button>
                </>
            ) : (
                <>
                    <p>Name: {details?.name}</p>
                    <p>Owner: {details?.owner}</p>
                    <p>Last Modified: {details?.lastModification ? details.lastModification.toLocaleDateString() : 'N/A'}</p>
                    <button onClick={() => setIsEditing(true)}>Rename</button>
                </>
            )}
            <div className="sharing-info">
                <h3>Shared With</h3>
                <div className="share-list">
                    {details?.sharedWith.map((share) => (
                        <div key={share.username} className="share-entry">
                            <span>{share.username}</span>
                            <select
                                value={share.accessLevel}
                                onChange={(e) => {
                                    setDetails({
                                        ...details,
                                        sharedWith: details.sharedWith.map((s) => {
                                            let level = 0;
                                            if(e.target.value == "read") level = 1;
                                            else level = 2;
                                            return s.username === share.username ? { ...s, accessLevel: level } : s
                                        }),
                                    });
                                    setShowSave(true);
                                }}
                            >
                                <option value="read">Read</option>
                                <option value="write">Write</option>
                            </select>
                            <button
                                onClick={() => {
                                    setDetails({
                                        ...details,
                                        sharedWith: details.sharedWith.filter((s) => s.username !== share.username),
                                    });
                                    setShowSave(true);
                                }}
                            >
                                Revoke
                            </button>
                        </div>
                    ))}
                </div>
                <button onClick={() => setShowSave(true)}>+ Add Share</button>
            </div>
            {showSave && <button onClick={handleRename}>Save Changes</button>}
            <button onClick={handleDelete}>Delete</button>
            <button onClick={() => refreshItems()}>Close</button>
        </div>
    );
};

export default FileDetailsSidebar;
