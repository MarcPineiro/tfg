import React, { useState, useRef, useEffect } from 'react';
import { useToast } from './ToastProvider';
import { AiOutlineFolder, AiOutlineFile } from 'react-icons/ai';
import '../css/ContextMenu.css';
import { Items } from '../utils/Items';  // Import the custom hook
import { Item } from '../public/types';
import {
    deleteItemAPI,
    getFileDetailsAPI,
    moveItemAPI,
    renameItemAPI,
  } from '../utils/api';

interface ContextMenuProps {
    xPos: number;
    yPos: number;
    show: boolean;
    currentFolderId: string;
    itemType: string;
    setShowContextMenu: (state: boolean) => void;
    contextMenu: {
        xPos: number,
        yPos: number,
        itemType: string,
        itemId: string | null,
        name: string | null
    }
    setHighlightedItem: ((state: Item | null) => void) | null;
    setRechargeItems: React.Dispatch<React.SetStateAction<boolean>>;
    setRenamingItem: ((state: { isRenaming: boolean, itemId: string}) => void) | null;
    currentContext: string;
    onContextMenuAction: (action:string) => void;
}

const ContextMenu: React.FC<ContextMenuProps> = ({
    xPos,
    yPos,
    show,
    currentFolderId,
    itemType,
    setShowContextMenu,
    contextMenu,
    setHighlightedItem,
    setRechargeItems,
    setRenamingItem,
    currentContext,
    onContextMenuAction, // Add this prop
}) => {
    const showToast = useToast();
    if (!show) return null;
    const {
        folderCreate,
        fileUpload,
        deleteItem,
    } = Items();

    const style = {
        top: `${yPos}px`,
        left: `${xPos}px`,
    };



    const handleFolderCreate = () => {
        try {
            folderCreate(currentFolderId, null);
            setRechargeItems(true);
            showToast('Folder created successfully!', 'info');
        } catch (error) {
            showToast('Failed to create folder.', 'error');
        }
        setShowContextMenu(false);
    };

    const handleFileUpload = (event: React.ChangeEvent<HTMLInputElement>) => {
        try {
            const uploadedFiles = event.target.files;
            fileUpload(uploadedFiles, currentFolderId);
            setRechargeItems(true);
            showToast('File uploaded successfully!', 'info');
        } catch (error) {
            showToast('Failed to upload file.', 'error');
        }
        setShowContextMenu(false);
    };

    const handleRenameItem = () => {
        if (setRenamingItem) {
            setRenamingItem({ isRenaming: true, itemId: contextMenu.itemId! });
            setShowContextMenu(false);
          }
    };

    const handleDelete = async (itemId: string, itemName: string) => {
        const confirmDelete = window.confirm(`Are you sure you want to delete ${itemName}?`);

        if (confirmDelete) {
            try {
                await deleteItem(itemId);
                setRechargeItems(true);
                setHighlightedItem!(null);
                showToast('Item deleted successfully!', 'info');
            } catch (error) {
                showToast('Failed to delete item.', 'error');
            }
        }
    };

    const handleDeleteItem = () => {
        if (contextMenu.itemId !== null && contextMenu.name !== null) {
            handleDelete(contextMenu.itemId, contextMenu.name!);
        }
        setShowContextMenu(false);
    };

    const handleDownload = async () => {
        try {
            const item = await getFileDetailsAPI(contextMenu.itemId!);
            const blob = new Blob([item.content], { type: item.mimeType });
            const url = URL.createObjectURL(blob);
            const link = document.createElement('a');
            link.href = url;
            link.download = item.name;
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            showToast('File downloaded successfully!', 'info');
        } catch (error) {
            showToast('Failed to download file.', 'error');
        }
        setShowContextMenu(false);
      };

      const handleMove = () => {
        onContextMenuAction('move');
        setShowContextMenu(false);
    };

    const handleShare = () => {
        onContextMenuAction('share');
        setShowContextMenu(false);
    };

    const handleDetails = () => {
        onContextMenuAction('details');
        setShowContextMenu(false);
    };

    return (
        <div className="context-menu"> 
        <ul>
            {currentContext === 'files' && (itemType === 'folder' || itemType === 'file') && (
                <>
                    <li onClick={handleRenameItem}><AiOutlineFolder className="icon" /> Rename</li>
                    <li onClick={handleDeleteItem}><AiOutlineFile className="icon" /> Delete</li>
                    <li onClick={handleDownload}><AiOutlineFile className="icon" /> Download</li>
                    <li onClick={handleMove}><AiOutlineFile className="icon" /> Move</li>
                    <li onClick={handleShare}><AiOutlineFile className="icon" /> Share</li>
                    <li onClick={handleDetails}><AiOutlineFile className="icon" /> Details</li>
                </>
            )}
            {currentContext === 'files' && !(itemType === 'folder' || itemType === 'file') && (
                <>
                    <li onClick={handleFolderCreate}><AiOutlineFolder className="icon" /> New Folder</li>
                    <li>
                        <label>
                            <AiOutlineFile className="icon" /> Upload File
                            <input type="file" style={{ display: 'none' }} onChange={handleFileUpload} />
                        </label>
                    </li>
                    <li>
                        <label>
                            <AiOutlineFile className="icon" /> Upload Folder
                            <input type="file" style={{ display: 'none' }} onChange={handleFileUpload} />
                        </label>
                    </li>
                </>
            )}
            {currentContext === 'shared' && (itemType === 'folder' || itemType === 'file') && (
                <>
                    <li onClick={() => {/* Implement Revoke Share */ }}>Revoke Share</li>
                    <li onClick={handleDetails}><AiOutlineFile className="icon" /> Details</li>
                </>
            )}
            {currentContext === 'trash' && (itemType === 'folder' || itemType === 'file') && (
                <>
                    <li onClick={() => {/* Implement Restore */ }}>Restore</li>
                    <li onClick={() => {/* Implement Permanent Delete */ }}>Delete Permanently</li>
                </>
            )}
        </ul>
    </div>
    );
};

export default ContextMenu;
