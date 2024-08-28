import React from 'react';
import { AiOutlineFileText, AiOutlineFilePdf, AiOutlineFileImage, AiOutlineFile } from 'react-icons/ai';
import { LiaFileVideoSolid, LiaFileAudioSolid } from "react-icons/lia";
import { FaRegFileArchive } from "react-icons/fa";
import { PiFileHtmlLight } from "react-icons/pi";
import { BsFiletypeExe } from "react-icons/bs";
import '../css/File.css';
import Element from './Element';
import { FileItem, Item } from '../public/types';

interface FileProps {
    file: FileItem;
    handleContextMenu: (e: React.MouseEvent, itemId: string) => void;
    highlightedItem: Item | null;
    handleHighlight: (item: Item) => void;
    setRechargeItems: React.Dispatch<React.SetStateAction<boolean>>;
    renamingItem: { isRenaming: boolean; itemId: string | null };
}

const getFileIcon = (fileType: string) => {
    switch (fileType) {
        case 'text':
            return <AiOutlineFileText className="file-icon" size={45} />;
        case 'pdf':
            return <AiOutlineFilePdf className="file-icon" size={45} />;
        case 'image':
            return <AiOutlineFileImage className="file-icon" size={45} />;
        case 'video':
            return <LiaFileVideoSolid className="file-icon" size={45} />;
        case 'audio':
            return <LiaFileAudioSolid className="file-icon" size={45} />;
        case 'archive':
            return <FaRegFileArchive className="file-icon" size={45} />;
        case 'web':
            return <PiFileHtmlLight className="file-icon" size={45} />;
        case 'executable':
            return <BsFiletypeExe className="file-icon" size={45} />;
        default:
            return <AiOutlineFile className="file-icon" size={45} />;
    }
};

const File: React.FC<FileProps> = ({ file, handleContextMenu, highlightedItem, handleHighlight, setRechargeItems, renamingItem }) => {
    return (
        <Element
            element={file}
            onContextMenu={handleContextMenu}
            itemType="file"
            highlightedItem={highlightedItem}
            handleHighlight={handleHighlight}
            setRechargeItems={setRechargeItems}
            renamingItem={renamingItem}
        >
            <div className="folder">
                {getFileIcon(file.type)}
            </div>
        </Element>
    );
};

export default File;
