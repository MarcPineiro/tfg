import { AiOutlineFolder } from 'react-icons/ai';
import '../css/Folder.css';
import Element from './Element';
import { Item, FolderItem } from '../public/types';
import { renameItemAPI } from '../utils/api';

interface FolderProps {
    folder: FolderItem;
    handleContextMenu: (e: React.MouseEvent, itemId: string) => void;
    highlightedItem: Item | null;
    handleHighlight: (item: Item) => void;
    setRechargeItems: React.Dispatch<React.SetStateAction<boolean>>;
    renamingItem: { isRenaming: boolean; itemId: string | null };
}

const Folder: React.FC<FolderProps> = ({ folder, handleContextMenu, highlightedItem, handleHighlight, setRechargeItems, renamingItem }) => {

    return (
        <Element
            element={folder}
            onContextMenu={handleContextMenu}
            itemType="folder"
            highlightedItem={highlightedItem}
            handleHighlight={handleHighlight}
            setRechargeItems={setRechargeItems}
            renamingItem={renamingItem}
        >
            <div className="folder">
                <AiOutlineFolder className="folder-icon" size={45}/>
            </div>
        </Element>
    );
};

export default Folder;
