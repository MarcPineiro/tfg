import React, { useState, useRef, useEffect } from 'react';
import { useToast } from './ToastProvider';
import { AiOutlineFolder, AiOutlineFile } from 'react-icons/ai';
import '../css/ContextMenu.css';
import { Items } from '../utils/Items';  // Import the custom hook
import { Item } from '../public/types';
import {
    deleteTrashItemAPI,
    getFileDetailsAPI,
    restoreTrashItemAPI,
    revokeShareAPI,
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
            setHighlightedItem!(null);
            setRechargeItems(true);
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
        setRechargeItems(true);
        setShowContextMenu(false);
    };

    const handleShare = () => {
        onContextMenuAction('share');
        setRechargeItems(true);
        setShowContextMenu(false);
    };

    const handleDetails = () => {
        onContextMenuAction('details');
        setRechargeItems(true);
        setShowContextMenu(false);
    };

    const handleRevoke = async () => {
        if (contextMenu.itemId !== null && contextMenu.name !== null) {
            try {
                await revokeShareAPI(contextMenu.itemId);
                setRechargeItems(true);
                setHighlightedItem!(null);
                showToast('Item deleted successfully!', 'info');
            } catch (error) {
                showToast('Failed to delete item.', 'error');
            }
        }
        setShowContextMenu(false);
    };

    const handleRestore = async () => {
        if (contextMenu.itemId !== null && contextMenu.name !== null) {
            try {
                await restoreTrashItemAPI(contextMenu.itemId);
                setRechargeItems(true);
                setHighlightedItem!(null);
                showToast('Item restored successfully!', 'info');
            } catch (error) {
                showToast('Failed to restore item.', 'error');
            }
        }
        setShowContextMenu(false);
    };

    const handleDeletePerma = async () => {
        if (contextMenu.itemId !== null && contextMenu.name !== null) {
            const confirmDelete = window.confirm(`Are you sure you want to completly delete ${contextMenu.name}?`);
            if (confirmDelete) {
                try {
                    await deleteTrashItemAPI(contextMenu.itemId);
                    setRechargeItems(true);
                    setHighlightedItem!(null);
                    showToast('Item deleted successfully!', 'info');
                } catch (error) {
                    showToast('Failed to delete item.', 'error');
                }
            }
        }
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
                    <li onClick={handleRevoke}>Revoke Share</li>
                </>
            )}
            {currentContext === 'trash' && (itemType === 'folder' || itemType === 'file') && (
                <>
                    <li onClick={() => {handleRestore}}>Restore</li>
                    <li onClick={() => {handleDeletePerma}}>Delete Permanently</li>
                </>
            )}
        </ul>
    </div>
    );
};

export default ContextMenu;
