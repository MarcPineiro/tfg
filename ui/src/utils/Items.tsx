import { useCallback, useState, useEffect, useMemo } from 'react'
import { Item, FolderItem, FileItem } from '../public/types';
import {
    getFolderApi,
    renameItemAPI,
    deleteItemAPI,
    uploadFileAPI,
    moveItemAPI,
    downloadFileAPI,
    getRootFolderAPI,
    getRootShareFolderAPI,
    createFileAPI,
    getRootTrashAPI
} from './api';

export const Items = () => {
    // const [items, setItems] = useState<Item[]>([]);
    // const [data, setData] = useState<folderStructureData>(mockFolderStructure);
    //const [currentFolderId, setCurrentFolderId] = useState<string | null>(null);

    const getData = async (folderId: string | null = null, currentContext: string, showToast?: (msg: string, toastType?: 'error' | 'info') => void) : Promise<FolderItem | null> => {

        const folderData = await getFolderApi(folderId, currentContext, showToast);
        return folderData;
    };

    const renameItem = async (itemId: string, newName: string, showToast?: (msg: string, toastType?: 'error' | 'info') => void) => {
        await renameItemAPI(itemId, newName, showToast);
    };

    const deleteItem = async (itemId: string, showToast?: (msg: string, toastType?: 'error' | 'info') => void) => {
        await deleteItemAPI(itemId, showToast);
    };

    const moveItem = async (itemId: string, newFolder: string, showToast?: (msg: string, toastType?: 'error' | 'info') => void) => {
        await moveItemAPI(itemId, newFolder, showToast);
    }

    const fileUpload = async (uploadedFiles: FileList | null, targetFolderId: string | null, showToast?: (msg: string, toastType?: 'error' | 'info') => void) => {
        if (uploadedFiles) {
            for (let i = 0; i < uploadedFiles.length; i++) {
                await uploadFileAPI(uploadedFiles[i], targetFolderId ?? '', showToast);
            }
        }
    };

    const folderCreate = async (targetFolderId: string, file:File | null, showToast?: (msg: string, toastType?: 'error' | 'info') => void) => {
        if(file) {
            createFileAPI({name: file.name, contentType: file.type, size: file.size, parentFolderId: targetFolderId, isFolder: true}, file, showToast);
        }
    };

    const downloadItem = async (itemId: string | null, showToast?: (msg: string, toastType?: 'error' | 'info') => void) => {
        if(itemId != null) {
            downloadFileAPI(itemId, showToast);
        }
    }

    const getItems = (folder: FolderItem | null): Item[] => {
        let folders:Item[] = [];
        let files:Item[] = [];
        if(folder) {
            if(folder.subfolders != null && folder.subfolders != undefined) folders = [...folder.subfolders]
            if(folder.files != null && folder.files != undefined) folders = [...folder.files]
        }
        
        return (sortItems([...folders, ...files]));
    }

    const getRootItems = async (showToast?: (msg: string, toastType?: 'error' | 'info') => void): Promise<FolderItem | null> => {
        return await getRootFolderAPI(showToast);
    }

    const getRootSharedItems = async (showToast?: (msg: string, toastType?: 'error' | 'info') => void): Promise<FolderItem | null> => {
        return await getRootShareFolderAPI(showToast);
    }

    const currentItems = async (folderId: string, currentContext: string, showToast?: (msg: string, toastType?: 'error' | 'info') => void): Promise<Item[] | null> => {
        if(folderId != null && folderId != undefined) return getItems(await getData(folderId, currentContext,showToast));
        else {
            if(currentContext == "trash") {
                return getItems(await getRootTrashAPI(showToast));
            } else if(currentContext == "shared") {
                return getItems(await getRootShareFolderAPI(showToast));
            }
            return getItems(await getRootFolderAPI(showToast)); //TODO mostrar error y actualizar structure
        }
    };

    const sortItems = (items: Item[]): Item[] => {
        return items.sort((a, b) => {
            if (a.type === 'folder' && b.type === 'file') return -1;
            if (a.type === 'file' && b.type === 'folder') return 1;
            return a.name.localeCompare(b.name);
        });
    }

    const getFileType = (fileName: string | null) => {
        const extension = fileName?.split('.').pop()?.toLowerCase() || "unknown";
        const fileTypeMap: { [key: string]: string } = {
            txt: "text",
            md: "text",
            doc: "text",
            docx: "text",
            rtf: "text",
            pdf: "pdf",
            jpg: "image",
            jpeg: "image",
            png: "image",
            gif: "image",
            bmp: "image",
            svg: "image",
            mp4: "video",
            avi: "video",
            mov: "video",
            mkv: "video",
            mp3: "audio",
            wav: "audio",
            aac: "audio",
            flac: "audio",
            zip: "archive",
            rar: "archive",
            "7z": "archive",
            tar: "archive",
            gz: "archive",
            html: "web",
            htm: "web",
            css: "web",
            js: "web",
            ts: "web",
            jsx: "web",
            tsx: "web",
            exe: "executable",
            dll: "executable",
            bin: "executable",
            msi: "executable"
        };
        return fileTypeMap[extension] || "unknown";
    }

    return useMemo(() => ({
        folderCreate,
        fileUpload,
        renameItem,
        deleteItem,
        getFileType,
        downloadItem,
        moveItem,
        currentItems,
        getRootItems,
        getRootSharedItems
    }), [folderCreate, fileUpload, renameItem, deleteItem, getFileType, downloadItem, moveItem, currentItems, getRootItems]);
};



    // const findItemIO = (itemId: string, folder: FolderItem): { item: Item | null, pernt: FolderItem | null } => {
    //     if (folder.subfolders.some(subfolder => subfolder.id === itemId)) {
    //         return { item: folder.subfolders.find(subfolder => subfolder.id === itemId) || null, pernt: folder };
    //     }
    //     if (folder.files.some(file => file.id === itemId)) {
    //         return { item: folder.files.find(file => file.id === itemId) || null, pernt: folder };
    //     }
    //     for (const subfolder of folder.subfolders) {
    //         const foundItem = findItemIO(itemId, subfolder);
    //         if (foundItem.item) return foundItem;
    //     }
    //     return { item: null, pernt: folder };
    // }

    // const findItem = (itemId: string): { item: Item | null, pernt: FolderItem | null } => {
    //     if(itemId == data.root.id) return { item: data.root, pernt: null };
    //     if(itemId == data.shared.id) return { item: data.shared, pernt: null };
    //     if(itemId == data.trash.id) return { item: data.trash, pernt: null };
    //     return findItemIO(itemId, data.root) || findItemIO(itemId, data.shared) || findItemIO(itemId, data.trash) || { item: null, pernt: null };
    // }

    // const addItemToFolderIO = (item: Item, targetFolderId: string | null, folder: FolderItem): FolderItem => {
    //     if (folder.id === targetFolderId) {
    //         if (item.type === 'folder') {
    //             return { ...folder, subfolders: [...folder.subfolders, item as FolderItem] };
    //         } else {
    //             return { ...folder, files: [...folder.files, item as FileItem] };
    //         }
    //     }
    //     return { ...folder, subfolders: folder.subfolders.map(subfolder => addItemToFolderIO(item, targetFolderId, subfolder)) };
    // }

    // const addItemToFolder = (item: Item, targetFolderId: string | null) => {
    //     if (!targetFolderId) targetFolderId = data.root.id;
    //     const updatedRoot = addItemToFolderIO(item, targetFolderId, data.root);
    //     const updatedShared = addItemToFolderIO(item, targetFolderId, data.shared);
    //     const updatedTrash = addItemToFolderIO(item, targetFolderId, data.trash);
    //     data = { root: updatedRoot, shared: updatedShared, trash: updatedTrash };
    //     setCurrentFolderId(targetFolderId); // Save the folderId to update later
    // }

    // const renameItemIO = (itemId: string, folderParentId: string, newName: string, folder: FolderItem): FolderItem => {
    //     if (folder.id === folderParentId) {
    //         return {
    //             ...folder,
    //             subfolders: folder.subfolders.map(subfolder => subfolder.id === itemId ? { ...subfolder, name: newName } : subfolder),
    //             files: folder.files.map(file => file.id === itemId ? { ...file, name: newName } : file),
    //         };
    //     }
    //     return { ...folder, subfolders: folder.subfolders.map(subfolder => renameItemIO(itemId, folderParentId, newName, subfolder)) };
    // }

    // const renameItem = (itemId: string, folderParentId: string, newName: string) => {
    //     const updatedRoot = renameItemIO(itemId, folderParentId, newName, data.root);
    //     const updatedShared = renameItemIO(itemId, folderParentId, newName, data.shared);
    //     const updatedTrash = renameItemIO(itemId, folderParentId, newName, data.trash);
    //     data = { root: updatedRoot, shared: updatedShared, trash: updatedTrash };
    //     setCurrentFolderId(folderParentId); // Save the folderId to update later
    // }

    // const deleteItemIO = (itemId: string, folderParentId: string, folder: FolderItem): FolderItem => {
    //     if (folder.id === folderParentId) {
    //         return {
    //             ...folder,
    //             subfolders: folder.subfolders.filter(subfolder => subfolder.id !== itemId),
    //             files: folder.files.filter(file => file.id !== itemId),
    //         };
    //     }
    //     return { ...folder, subfolders: folder.subfolders.map(subfolder => deleteItemIO(itemId, folderParentId, subfolder)) };
    // }

    // const deleteItem = (itemId: string, folderParentId: string | null) => {
    //     const updatedRoot = deleteItemIO(itemId, folderParentId ?? data.root.id, data.root);
    //     const updatedShared = deleteItemIO(itemId, folderParentId ?? data.shared.id, data.shared);
    //     const updatedTrash = deleteItemIO(itemId, folderParentId ?? data.trash.id, data.trash);
    //     data = { root: updatedRoot, shared: updatedShared, trash: updatedTrash };
    //     setCurrentFolderId(folderParentId); // Save the folderId to update later
    // }

    // useEffect(() => {
    //     setTimeout(() => {
    //         updateItems();
    //     }, 0);
    //     items;

    // }, [data]); // updateItems will run whenever data or currentFolderId changes

    // const fileUpload = (uploadedFiles: FileList | null, targetFolderId: string | null) => {
    //     if (uploadedFiles) {
    //         for (let i = 0; i < uploadedFiles.length; i++) {
    //             const newFile: FileItem = {
    //                 name: uploadedFiles[i].name,
    //                 id: (items.length + 1000 + i + 1).tostring(),
    //                 type: getFileType(uploadedFiles[i].name),
    //             };
    //             addItemToFolder(newFile, targetFolderId);
    //         }
    //     }
    // }