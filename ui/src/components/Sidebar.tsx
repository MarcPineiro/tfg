import React, { useState, useEffect, useRef } from 'react';
import { RiAddLine } from 'react-icons/ri';
import '../css/Sidebar.css';
import ContextMenu from './ContextMenu';
import FolderStructure from './FolderStructure'
import { FolderItem, Item } from '../public/types';
import { Items } from '../utils/Items';

interface SidebarProps {
    onFolderSelect: (folder: FolderItem, structure: string) => void;
    selectedFolder: FolderItem | null;
    root: FolderItem;
    shared: FolderItem;
    trash: FolderItem;
    setSelectedFolder: React.Dispatch<React.SetStateAction<FolderItem | null>>;
    setRechargeItems: React.Dispatch<React.SetStateAction<boolean>>;
    isFolderExpanded: (folderId: string) => boolean;
    toggleFolderExpanded: (folderId: string) => void;
    setCurrentContext: React.Dispatch<React.SetStateAction<string>>
    currentContext: string
}

const Sidebar: React.FC<SidebarProps> = ({
    onFolderSelect,
    selectedFolder,
    setSelectedFolder,
    setRechargeItems, 
    root,
    shared,
    trash,
    toggleFolderExpanded,
    isFolderExpanded,
    setCurrentContext, 
    currentContext
}) => {
    const [showContextMenu, setShowContextMenu] = useState(false);
    const [contextMenuPosition, setContextMenuPosition] = useState({ xPos: 0, yPos: 0 });
    const contextMenuRef = useRef<HTMLDivElement>(null);
    const [folderStructure, setFolderStructure] = useState([]);

    const handleNewButtonClick = (e: React.MouseEvent) => {
        e.preventDefault();
        setShowContextMenu(!showContextMenu);
        setContextMenuPosition({ xPos: e.clientX, yPos: e.clientY });
    };

    const handleFolderClick = (folder: FolderItem, contex: string) => {
        if (selectedFolder !== null && selectedFolder.id !== folder.id) {
            onFolderSelect(folder, contex);
            setSelectedFolder(selectedFolder);
            setCurrentContext(contex);
        }
    };

    useEffect(() => {
        const handleClickOutside = (e: MouseEvent) => {
            if (contextMenuRef.current && !contextMenuRef.current.contains(e.target as Node)) {
                setShowContextMenu(false);
            }
        };

        document.addEventListener('mousedown', handleClickOutside);

        return () => {
            document.removeEventListener('mousedown', handleClickOutside);
        };
    }, []);

    const empty = (action:string) => {

    }

    return (
        <div className="sidebar">
            <button className="add-new" onClick={handleNewButtonClick}>
                <RiAddLine className="icon" />
                Nuevo
            </button>
            {showContextMenu && (
                <div ref={contextMenuRef}>
                    <ContextMenu
                        xPos={contextMenuPosition.xPos}
                        yPos={contextMenuPosition.yPos}
                        show={showContextMenu}
                        currentFolderId={(selectedFolder??root).id}
                        itemType=""
                        setShowContextMenu={setShowContextMenu}
                        contextMenu={{xPos:0, yPos:0, itemType:'', itemId:null, name:null}}
                        setHighlightedItem={null}
                        setRechargeItems={setRechargeItems}
                        setRenamingItem={null}
                        currentContext={currentContext}
                        onContextMenuAction={empty}
                    />
                </div>
            )}
            <div onClick={() => handleFolderClick(root, "files")}> 
                <FolderStructure 
                    structureData={root} 
                    onFolderClick={onFolderSelect} 
                    setRechargeItems={setRechargeItems}
                    root='files'
                    toggleFolderExpanded={toggleFolderExpanded}
                    isFolderExpanded={isFolderExpanded}
                />
            </div>
            <div onClick={() => handleFolderClick(shared, "share")}>
                <FolderStructure 
                    structureData={shared} 
                    onFolderClick={onFolderSelect}
                    setRechargeItems={setRechargeItems}
                    root='share'
                    toggleFolderExpanded={toggleFolderExpanded}
                    isFolderExpanded={isFolderExpanded}
                />
            </div>
            <div onClick={() => handleFolderClick(trash, "trash")}>
                <FolderStructure 
                    structureData={trash} 
                    onFolderClick={onFolderSelect}
                    setRechargeItems={setRechargeItems}
                    root='trash'
                    toggleFolderExpanded={toggleFolderExpanded}
                    isFolderExpanded={isFolderExpanded}
                />
            </div>
        </div>
    );
};

export default Sidebar;