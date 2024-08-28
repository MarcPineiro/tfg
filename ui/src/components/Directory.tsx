import React, { useState, useRef, useEffect } from 'react';
import '../css/FolderStructure.css';
import { Items } from '../utils/Items';
import { useDrop } from 'react-dnd';
import { Item, FolderItem, DraggableItem } from '../public/types';

interface DirectoryProps {
    folder: FolderItem
    children?: React.ReactNode;
    onClick: (folder: FolderItem, structure: string) => void;
    setRechargeItems: React.Dispatch<React.SetStateAction<boolean>>;
    root: string
    isFolderExpanded: (folderId: string) => boolean;
    toggleFolderExpanded: (folderId: string) => void;
}

const Directory: React.FC<DirectoryProps> = ({ folder, children, onClick, root, toggleFolderExpanded, isFolderExpanded, setRechargeItems }) => {
    const [isExpanded, setIsExpanded] = useState(false);
    const { moveItem } = Items();

    const [, drop] = useDrop({
        accept: ['element'],
        drop: (item: DraggableItem) => {
            moveItem(item.id, folder.id);
            setRechargeItems(true);
        },
        collect: (monitor) => ({
            isOver: monitor.isOver(),
        }),
    });

    const handleToggle = () => {
        toggleFolderExpanded(folder.id);
    };

    const handleClick = () => {
        if(!isFolderExpanded(folder.id)) toggleFolderExpanded(folder.id);
        onClick(folder, root);
    };

    return (
        <div className="folder-structure" style={{ paddingLeft: '15px' }}>
            <div className="folder-row">
                <span onClick={handleToggle} style={{ cursor: 'default', marginRight: '5px' }}>
                    {isFolderExpanded(folder.id) ? '▼' : '▶'}
                </span>
                <span onClick={handleClick} style={{ cursor: 'pointer', userSelect: 'none' }}>
                    {folder.name}
                </span>
            </div>
            <div style={{ display: isFolderExpanded(folder.id) ? 'block' : 'none' }} ref={drop}>
                {children}
            </div>
        </div>
    );
}

//     return (
//         <div
//             className="folder-structure"
//             style={{
//                 paddingLeft: `15px`, // Indent based on the level
//             }}
//         >
//             <div
//                 onClick={() => {
//                     handleToggle();
//                     handleClick();
//                 }}
//                 style={{
//                     cursor: 'pointer',
//                     userSelect: 'none',
//                 }}
//             >
//                 {isExpanded ? '▼' : '▶'} {folder.name}
//             </div>
//             <div style={{ display: isExpanded ? 'block' : 'none' }} ref={drop}>
//                 {children}
//             </div>
//         </div>
//     );
// };

export default Directory;