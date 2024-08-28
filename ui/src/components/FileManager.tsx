import React, { useState, useRef, useEffect } from 'react';
import { useToast } from './ToastProvider';
import '../css/FileManager.css';
import Folder from './Folder';
import File from './File';
import ContextMenu from './ContextMenu';
import { AiOutlineAppstore, AiOutlineUnorderedList } from 'react-icons/ai';
import Sidebar from './Sidebar';
import Header from './Header';
import { Items } from '../utils/Items';
import { getFolderApi, deleteUserAPI, logoutUser } from '../utils/api';
import { Item, FolderItem, FileItem } from '../public/types';
import { getRootTrashAPI } from '../utils/api';
import MoveItemModal from './MoveItemModal'; 
import ShareModal from './ShareModal'; 
import UserSettings from './UserSettings';
import FileDetailsSidebar from './FileDetailsSidebar';

import useWebSocketManager from '../hooks/useWebSocketManager';

const FileManager: React.FC = () => {
    const [isGridView, setIsGridView] = useState(true);
    const [highlightedItem, setHighlightedItem] = useState<Item | null>(null);
    const [showContextMenu, setShowContextMenu] = useState(false);
    const contextMenuRef = useRef<HTMLDivElement>(null);
    const gridContainerRef = useRef<HTMLDivElement>(null);
    const [rechargeItems, setRechargeItems] = useState(false);
    const [items, setItems] = useState<Item[]>([]);
    const [currentFolder, setCurrentFolder] = useState<FolderItem | null>(null);
    const [currentContext, setCurrentContext] = useState<string>("files");
    const [expandedFolders, setExpandedFolders] = useState<{ [folderId: string]: boolean }>({});
    
    const [selectedItem, setSelectedItem] = useState<Item | null>(null);
    const [showMoveModal, setShowMoveModal] = useState(false); 
    const [showShareModal, setShowShareModal] = useState(false); 
    const [showFileDetails, setShowFileDetails] = useState(false);
    const [showUserSettings, setShowUserSettings] = useState(false);
    const showToast = useToast();

    const [contextMenu, setContextMenu] = useState({
        xPos: 0,
        yPos: 0,
        itemType: '',
        itemId: null as string | null,
        name: null as string | null,
    });
    const [renamingItem, setRenamingItem] = useState({
        isRenaming: false,
        itemId: null as string | null,
    });
    const {
        deleteItem,
        downloadItem,
        currentItems,
        getRootItems,
        getRootSharedItems,
        moveItem
    } = Items();
    const [root, setRoot] = useState<FolderItem>();
    const [trash, setTrash] = useState<FolderItem>();
    const [shared, setShared] = useState<FolderItem>();

    const initializeFolders = async () => {
        try {
            const rootItems = await getRootItems();
            const trashItems = await getRootTrashAPI();
            const sharedItems = await getRootSharedItems();

            if(!(rootItems || trashItems || sharedItems)) {
                logoutUser();
            }
        
            setRoot(rootItems!);
            setTrash(trashItems!);
            setShared(sharedItems!);
        
            if (rootItems) {
                setCurrentFolder(rootItems);
                showToast('Folders initialized successfully.', 'info');
            } else {
                showToast("Failed to initialize root items. 'rootItems' is undefined.", 'error');
            }
        
            setRechargeItems(true);
        
            useWebSocketManager(refreshItems, () => {
                localStorage.clear();
                window.location.reload();
            });
        } catch(error) {
            showToast('Failed to initialize folders.', 'error');
        }
    }

    useEffect(() => {
        initializeFolders();
    }, []);

    const refreshItems = async (parentFolderId: string) => {
        if (parentFolderId === currentFolder?.id) {
            try {
                await currentItems(currentFolder.id, currentContext);
                showToast('Items refreshed successfully!', 'info');
            } catch (error) {
                showToast('Failed to refresh items.', 'error');
            }
        }
    };

    const handleMove = (targetFolderId: string) => {
        if (selectedItem) {
            moveItem(selectedItem.id, targetFolderId);
            setRechargeItems(true);
            setShowMoveModal(false);
        }
    };

    const handleContextMenuAction = (action: string) => {
        switch (action) {
            case 'move':
                setShowMoveModal(true);
                break;
            case 'share':
                setShowShareModal(true);
                break;
            case 'details':
                setShowFileDetails(true);
                break;
            default:
                break;
        }
    };
    


    useEffect(() => {
        const handleClickOutside = (e: MouseEvent) => {
            const clickedElement = e.target as HTMLElement;
            const element = gridContainerRef.current?.querySelector('.element');
            
            if (element && !element.contains(clickedElement)) {
                setHighlightedItem(null);
            }

            if (contextMenuRef.current && !contextMenuRef.current.contains(e.target as Node)) {
                setShowContextMenu(false);
            }
        };

        document.addEventListener('mousedown', handleClickOutside);

        return () => {
            document.removeEventListener('mousedown', handleClickOutside);
        };
    }, []);

    useEffect(() => {
        const handleKeyDown = (e: KeyboardEvent) => {
            if (e.key === 'ArrowUp') {
                moveHighlightUp();
            } else if (e.key === 'ArrowDown') {
                moveHighlightDown();
            } else if (e.key === 'ArrowLeft' && isGridView) {
                moveHighlightLeft();
            } else if (e.key === 'ArrowRight' && isGridView) {
                moveHighlightRight();
            } else if (e.key === 'Enter' && highlightedItem !== null) {
                downloadItem(highlightedItem.id);
            } else if (e.key === 'Delete' && highlightedItem !== null) {
                deleteItem(highlightedItem.id);
                setRechargeItems(true);
            }
        };

        document.addEventListener('keydown', handleKeyDown);
        return () => {
            document.removeEventListener('keydown', handleKeyDown);
        };
    }, [highlightedItem, isGridView]);

    const recharge = () => {
        if(currentFolder == null) {
            getRootItems();
        } else {
            currentItems(currentFolder!.id, currentContext);
        }
    }

    useEffect(() => {
        if (rechargeItems) {
            recharge();
            setRechargeItems(false);
        }
    }, [rechargeItems]);

    const handleHighlight = (item: Item) => {
        setHighlightedItem(item);
    };
    
    const handleItemSelect = (item: Item) => {
        setSelectedItem(item);
    };

    const moveHighlightUp = () => {
        if(highlightedItem == null) return;
        if (items.length === 0) return;
        const currentIndex = items.findIndex(item => item.id === highlightedItem!.id);

        if (isGridView) {
            const itemsPerRow = calculateItemsPerRow();
            const newIndex = currentIndex - itemsPerRow;
            if (newIndex >= 0) {
                setHighlightedItem(items[newIndex]);
            }
            else {
                setHighlightedItem(items[0]);
            }
        } else {
            const prevIndex = currentIndex > 0 ? currentIndex - 1 : items.length - 1;
            setHighlightedItem(items[prevIndex]);
        }
    };

    const moveHighlightDown = () => {
        if(highlightedItem == null) return;
        if (items.length === 0) return;
        const currentIndex = items.findIndex(item => item.id === highlightedItem.id);

        if (isGridView) {
            const itemsPerRow = calculateItemsPerRow();
            const newIndex = currentIndex + itemsPerRow;
            if (newIndex < items.length) {
                setHighlightedItem(items[newIndex]);
            }
            else {
                setHighlightedItem(items[items.length - 1]);
            }
        } else {
            const nextIndex = currentIndex < items.length - 1 ? currentIndex + 1 : 0;
            setHighlightedItem(items[nextIndex]);
        }
    };

    const moveHighlightLeft = () => {
        if (isGridView) {
            if(highlightedItem == null) return;
            if (items.length === 0) return;
            const currentIndex = items.findIndex(item => item.id === highlightedItem.id);
            const newIndex = currentIndex > 0 ? currentIndex - 1 : 0;
            setHighlightedItem(items[newIndex]);
        }
    };

    const moveHighlightRight = () => {
        if (isGridView) {
            if(highlightedItem == null) return;
            if (items.length === 0) return;
            const currentIndex = items.findIndex(item => item.id === highlightedItem.id);
            const newIndex = currentIndex < items.length - 1 ? currentIndex + 1 : items.length - 1;
            setHighlightedItem(items[newIndex]);
        }
    };

    const calculateItemsPerRow = () => {
        if (!gridContainerRef.current) return 1;
        const gridWidth = gridContainerRef.current.offsetWidth;
        const itemWidth = gridContainerRef.current.querySelector('.element')?.clientWidth || gridWidth;
        return Math.floor(gridWidth / itemWidth);
    };

    const handleContextMenu = (
        e: React.MouseEvent,
        itemType: string,
        itemId: string | null,
        name: string | null
    ) => {
        e.preventDefault();
        e.stopPropagation();
        setContextMenu({
            xPos: e.clientX,
            yPos: e.clientY,
            itemType: itemType,
            itemId: itemId,
            name: name
        });
        setShowContextMenu(true);
        const item = items.filter(it => it.id == itemId);
        handleItemSelect(item[0]);
    };

    const toggleView = () => {
        setIsGridView(!isGridView);
    };

    const handleGridContextMenu = (e: React.MouseEvent) => {
        e.preventDefault();
        setContextMenu({
            xPos: e.clientX,
            yPos: e.clientY,
            itemType: '',
            itemId: null,
            name: null
        });
        setShowContextMenu(true);
    };

    const loadFolderItems = (folder: FolderItem, structure: string) => {
        setCurrentFolder(folder);
        setCurrentContext(structure);
        setRechargeItems(true);
    };

    const toggleFolderExpanded = (folderId: string) => {
        setExpandedFolders((prevState) => ({
            ...prevState,
            [folderId]: !prevState[folderId],
        }));
    };

    const isFolderExpanded = (folderId: string) => {
        return !!expandedFolders[folderId];
    };

    const handleUserSettings = () => {
        setShowUserSettings(true); // Show the User Settings modal
    };

    return (
        <div className="file-manager">
            <Sidebar
                onFolderSelect={loadFolderItems}
                selectedFolder={currentFolder}
                setSelectedFolder={setCurrentFolder}
                setRechargeItems={setRechargeItems}
                root={root!}
                trash={trash!}
                shared={shared!}
                toggleFolderExpanded={toggleFolderExpanded}
                isFolderExpanded={isFolderExpanded}
                setCurrentContext={setCurrentContext}
                currentContext={currentContext}
            />
            <div className="main-content" onContextMenu={handleGridContextMenu} > 
            {/* ref={drop} */}
            <Header handleUserSettings={handleUserSettings} /> 
                <div className="file-manager-header">
                    <div className="header-left">
                        <button className="btn btn-active">Archivos</button>
                        <button className="btn">Carpetas</button>
                    </div>
                    <div className="header-right">
                        <div className="view-toggle">
                            <AiOutlineAppstore className={`view-icon ${isGridView ? 'active' : ''}`} onClick={toggleView} />
                            <AiOutlineUnorderedList className={`view-icon ${!isGridView ? 'active' : ''}`} onClick={toggleView} />
                        </div>
                    </div>
                </div>
                {showContextMenu && (
                    <div ref={contextMenuRef} onContextMenu={handleGridContextMenu}>
                        <ContextMenu
                            xPos={contextMenu.xPos}
                            yPos={contextMenu.yPos}
                            show={showContextMenu}
                            currentFolderId={(currentFolder ?? root!).id}
                            itemType={contextMenu.itemType}
                            setShowContextMenu={setShowContextMenu}
                            contextMenu={contextMenu}
                            setHighlightedItem={setHighlightedItem}
                            setRechargeItems={setRechargeItems}
                            setRenamingItem={setRenamingItem}
                            currentContext={currentContext}
                            onContextMenuAction={handleContextMenuAction} // Pass action handler
                        />
                    </div>
                )}
                <div className={`file-manager-content ${isGridView ? 'grid-view' : 'list-view'}`} ref={gridContainerRef}>
                    {items.map((item) => {
                        if (item.type === 'folder') {
                            return (
                                <Folder
                                    key={item.id}
                                    folder={item as FolderItem}
                                    handleContextMenu={(e, folderId) => handleContextMenu(e, 'folder', folderId, item.name)}
                                    renamingItem={renamingItem}
                                    highlightedItem={highlightedItem}
                                    handleHighlight={handleHighlight}
                                    setRechargeItems={setRechargeItems}
                                />
                            );
                        } else {
                            return (
                                <File
                                    key={item.id}
                                    file={item as FileItem}
                                    handleContextMenu={(e, fileId) => handleContextMenu(e, 'folder', fileId, item.name)}
                                    renamingItem={renamingItem}
                                    highlightedItem={highlightedItem}
                                    handleHighlight={handleHighlight}
                                    setRechargeItems={setRechargeItems}
                                />
                            );
                        }
                    })}
                </div>
            </div>
            {/* Show Move Modal */}
            {showMoveModal && (
                <MoveItemModal
                    onClose={() => setShowMoveModal(false)}
                    onMove={handleMove}
                    structureData={root!}
                />
            )}

            {/* Show Share Modal */}
            {showShareModal && selectedItem && (
                <ShareModal
                    item={selectedItem}
                    onClose={() => setShowShareModal(false)}
                    onSuccess={() => setRechargeItems(true)}
                />
            )}

            {/* Show File Details Sidebar */}
            {showFileDetails && selectedItem && (
                <FileDetailsSidebar
                    item={selectedItem}
                    refreshItems={() => setRechargeItems(true)}
                />
            )}
        </div>
    );
};

export default FileManager;

// const selectFolder = ( folderId: string, folder: FolderItem ): FolderItem | undefined => {
//     if(folder.id == folderId) return folder;
//     if(folder.subfolders.length <= 0) return undefined;
//     var item = folder.subfolders.find(item => item.id == folderId);
//     if (item != undefined) return item;
//     for (let i = 0; i < folder.subfolders.length; i++) {
//         item = selectFolder(folderId, folder.subfolders[i]);
//         if(item != undefined) return item;
//     }
//     return undefined;
// }

// const selectFoder = (folderId: string, array: folderStructureData) : FolderItem => {
//     var item = selectFolder(folderId, array.root);
//     if(item == undefined) {
//         item = selectFolder(folderId, array.shared);
//         if(item == undefined) {
//             item = selectFolder(folderId, array.trash);
//         }
//     }
//     if(item != undefined) return item;
//     return data.root;
// }