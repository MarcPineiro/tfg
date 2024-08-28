import React, { useState, useRef, useEffect } from 'react';
import { useDrag } from 'react-dnd';
import '../css/Element.css';
import { useDrop } from 'react-dnd';
import { Items } from '../utils/Items';
import { DraggableItem, Item } from '../public/types';

interface ElementProps {
    element: Item
    children: React.ReactNode;
    onContextMenu: (e: React.MouseEvent, itemId: string) => void;
    renamingItem: { isRenaming: boolean; itemId: string | null };
    itemType: string;
    highlightedItem: Item | null;
    handleHighlight: (itemId: Item) => void;
    setRechargeItems: React.Dispatch<React.SetStateAction<boolean>>;
}

const Element: React.FC<ElementProps> = ({ element, children, onContextMenu, renamingItem, itemType, highlightedItem, handleHighlight, setRechargeItems }) => {
    const [newName, setNewName] = useState(element.name);
    const inputRef = useRef<HTMLInputElement>(null);
    const { moveItem, renameItem } = Items();
    const ref = useRef(null);

    const [{ isDragging }, drag] = useDrag({
        type: 'element',
        item: element,
        collect: (monitor) => ({
            isDragging: monitor.isDragging(),
        }),
    });

    const [, drop] = useDrop({
        accept: ['element'],
        drop: (item: DraggableItem) => {
            moveItem(item.id, element.id);
            setRechargeItems(true);
        },
        collect: (monitor) => ({
            isOver: monitor.isOver(),
        }),
    });

    useEffect(() => {
        element.name = newName;
        renameItem(element.id, newName);
    }, [newName]);

    const handleClick = () => {
        handleHighlight(element);
    };

    if(itemType == 'folder') {
        drag(drop(ref));
    } else {
        drag(ref);
    }

    return (
        <div
            ref={ref} 
            className={`element ${highlightedItem != null && highlightedItem.id === element.id ? 'highlighted' : ''} ${isDragging ? 'dragging' : ''}`}
            onClick={handleClick}
            onContextMenu={(e) => onContextMenu(e, element.id)}
        >
            <div className="icon-container">
                {children}
                {renamingItem.isRenaming && renamingItem.itemId === element.id ? (
                    <form onSubmit={(e) => {
                        e.preventDefault();
                        setNewName(newName);
                    }}>
                        <input
                            ref={inputRef}
                            value={newName}
                            onChange={(e) => setNewName(e.target.value)}
                            onBlur={(e) => setNewName(e.target.value)}
                            className="element-name-input"
                        />
                    </form>
                ) : (
                    <span className="element-name">{element.name}</span>
                )}
            </div>
        </div>
    );
};

export default Element;
