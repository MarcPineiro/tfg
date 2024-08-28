// Item interface for general file/folder items
export interface Item {
    id: string;
    name: string;
    type: string | 'folder';
    ownerId: string
}

// FileItem interface extending Item for file-specific properties
export interface FileItem extends Item {
    type: string;
    creationDate: Date;
    lastModification: Date;
}

// FolderItem interface extending Item for folder-specific properties
export interface FolderItem extends Item {
    type: 'folder';
    subfolders: FolderItem[];
    files: FileItem[];
    creationDate: Date;
    lastModification: Date;
}

// DraggableItem interface extending Item for draggable items
export interface DraggableItem extends Item {
    type: string;
    sourceFolderId: string | null;
}

// FolderStructureData interface for representing folder structure
export interface FolderStructureData {
    root: FolderItem;
    shared: FolderItem;
    trash: FolderItem;
}

// UserInfo interface for user data
export interface UserInfo {
    id: string;
    email: string;
    firstName: string;
    lastName: string;
    createdDate: Date;
    lastModifiedDate: Date;
}

// UserLogin interface for user login information
export interface UserLogin {
    username: string;
    password: string;
}

// UserRegister interface for user registration information
export interface UserRegister {
    username: string;
    password: string;
    email: string;
    firstName: string;
    lastName: string;
}

// CreateFileRequest interface for creating a new file
export interface CreateFileRequest {
    name: string;
    contentType: string;
    size: number;
    parentFolderId: string;
    isFolder: boolean;
}

// UpdateFileRequest interface for updating an existing file
export interface UpdateFileRequest {
    name: string;
}

// CreateFolderRequest interface for creating a new folder
export interface CreateFolderRequest {
    name: string;
    parentFolderId: string;
}

// UpdateFolderRequest interface for updating an existing folder
export interface UpdateFolderRequest {
    name: string;
}

// ShareRequest interface for sharing or revoking access to items
export interface ShareRequest {
    elementId: string;
    shareUsername: string;
    accessLevel: number
}

// UserRequest interface for creating or updating user information
export interface UserRequest {
    email: string;
    firstName: string;
    lastName: string;
}

// FolderInfo interface for detailed folder information with subfolders and files
export interface FolderInfo {
    id: string;
    name: string;
    creationDate: Date;
    lastModification: Date;
    subfolders: FileInfo[];
    files: FileInfo[];
}

// HistoryRecord interface for history records
export interface HistoryRecord {
    id: string;
    userId: string;
    action: string;
    elementType: string;
    elementName: string;
    elementId: string;
    parentId: string;
    path: string;
    actionDate: Date;
}

// FolderStructure interface for folder hierarchy
export interface FolderStructure {
    id: string;
    name: string;
    subfolders: FolderStructure[];
}

// UserResponse interface for API responses related to users
export interface UserResponse {
    email: string;
    firstName: string;
    lastName: string;
}

// FileInfo interface for file information
export interface FileInfo {
    id: string;
    name: string;
    creationDate: Date;
    lastModification: Date;
    owner: string
    sharedWith: SharedInfo[]
}
export interface SharedInfo {
    userId: string
    username: string;
    accessLevel: number;
}
