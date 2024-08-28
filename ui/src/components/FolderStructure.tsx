import React, { useState, useRef, useEffect } from 'react';
import '../css/FolderStructure.css';
import { Items } from '../utils/Items';
import { useDrop } from 'react-dnd';
import { Item, FolderItem, DraggableItem } from '../public/types';
import Directory from './Directory'

interface FolderStructureProps {
    structureData: FolderItem;
    onFolderClick: (folder: FolderItem, structure: string) => void;
    setRechargeItems: React.Dispatch<React.SetStateAction<boolean>>;
    root: string
    isFolderExpanded: (folderId: string) => boolean;
    toggleFolderExpanded: (folderId: string) => void;
}

const FolderStructure: React.FC<FolderStructureProps> = ({ structureData, onFolderClick, setRechargeItems, root, isFolderExpanded, toggleFolderExpanded }) => {
    const renderContent = (item: FolderItem) => {
        if (item == null || item == undefined) {
            return null; // Prevents attempting to render if item or subfolders are undefined
        } else if(item.subfolders == null || item.subfolders == undefined) {
            return (
            <Directory
                folder={item}
                key={item.id}
                onClick={onFolderClick}
                setRechargeItems={setRechargeItems}
                root={root}
                isFolderExpanded={isFolderExpanded}
                toggleFolderExpanded={toggleFolderExpanded}
            ></Directory>
            );
        }
        return (
            <Directory
                folder={item}
                key={item.id}
                onClick={onFolderClick}
                setRechargeItems={setRechargeItems}
                root={root}
                isFolderExpanded={isFolderExpanded}
                toggleFolderExpanded={toggleFolderExpanded}
            >
                {item.subfolders.map((subItem) => renderContent(subItem))}
            </Directory>
        );
    };

    if (!structureData) {
        return null; // Prevents rendering the component if structureData is undefined
    }

    return (
        <div className="folder-structure-container">
            {renderContent(structureData)}
        </div>
    );
};

export default FolderStructure;
