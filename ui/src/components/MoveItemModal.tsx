import React, { useState } from 'react';
import { FolderItem } from '../public/types';
import FolderStructure from './FolderStructure';
import './MoveItemModal.css';

interface MoveItemModalProps {
    onClose: () => void;
    onMove: (targetFolderId: string) => void;
    structureData: FolderItem;
}

const MoveItemModal: React.FC<MoveItemModalProps> = ({ onClose, onMove, structureData }) => {
    const [selectedFolderId, setSelectedFolderId] = useState<string | null>(null);

    const handleMove = () => {
        if (selectedFolderId) {
            onMove(selectedFolderId);
        }
    };

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                <h3>Move Item</h3>
                <FolderStructure
                    structureData={structureData}
                    onFolderClick={(folder) => setSelectedFolderId(folder.id)}
                    setRechargeItems={() => {}}
                    root=""
                    isFolderExpanded={() => false}
                    toggleFolderExpanded={() => {}}
                />
                <button onClick={handleMove} disabled={!selectedFolderId}>Move</button>
                <button onClick={onClose}>Cancel</button>
            </div>
        </div>
    );
};

export default MoveItemModal;
