import { API_CONFIG } from './config';
import { FolderItem, UserInfo, CreateFileRequest, HistoryRecord, ShareRequest, UserRequest, UserLogin, UserRegister, FolderStructure } from '../public/types';
import { useToast } from '../components/ToastProvider';

// Utility function to get the API URL
const getApiUrl = (endpoint: string): string => {
    const { host, port } = API_CONFIG;
    return port ? `http://${host}:${port}${endpoint}` : `http://${host}${endpoint}`;
};

// Utility function to handle token refresh
const fetchWithTokenRefresh = async (input: RequestInfo, init?: RequestInit, showToast?: (msg: string, toastType?: 'error' | 'info') => void): Promise<Response> => {
    let accessToken = localStorage.getItem('accessToken');

    let response = await fetch(input, {
        ...init,
        headers: {
            ...init?.headers,
            'Authorization': `Bearer ${accessToken}`,
            'X-Client-Type': 'web'
        },
    });

    if (response.status === 401) {
        try {
            await refreshTokenAPI(useToast);
            accessToken = localStorage.getItem('accessToken'); // Get the new token
            response = await fetch(input, {
                ...init,
                headers: {
                    ...init?.headers,
                    'Authorization': `Bearer ${accessToken}`,
                    'X-Client-Type': 'web'
                },
            });
        } catch (error) {
            if(showToast) showToast('Failed to refresh token. Please log in again.', 'error');
        }
    }

    return response;
};


export const checkTokenAPI = async (showToast?: (msg: string, toastType?: 'error' | 'info') => void): Promise<boolean> => {
    const accessToken = localStorage.getItem('accessToken');
    if (!accessToken) return false;

    const response = await fetchWithTokenRefresh(getApiUrl('/users/auth/check'), {
        method: 'POST',
    });

    if (!response.ok) {
        if(showToast) showToast('Failed to validate token.', 'error');
    }

    return response.ok;
};


export const getUserInfoAPI = async (showToast?: (msg: string, toastType?: 'error' | 'info') => void) => {
    const response = await fetchWithTokenRefresh(getApiUrl('/users'), {
        method: 'GET',
    });

    if (!response.ok) {
        if(showToast) showToast('Failed to fetch user info', "error");
        return null;
    }

    return response.json();
};

export const updateUserInfoAPI = async (userInfo: UserInfo, showToast?: (msg: string, toastType?: 'error' | 'info') => void) => {
    const response = await fetchWithTokenRefresh(getApiUrl('/users'), {
        method: 'PUT',
        body: JSON.stringify(userInfo),
    });

    if (!response.ok) {
        if(showToast) showToast('Failed to update user info', "error");
        return null;
    }

    return response.json();
};

export const changePasswordAPI = async (password: string, showToast?: (msg: string, toastType?: 'error' | 'info') => void) => { //TODO: añadir al servidor
    const response = await fetchWithTokenRefresh(getApiUrl('/users/auth/change-password'), {
        method: 'POST',
        body: JSON.stringify({ password }),
    });

    if (!response.ok) {
        if(showToast) showToast('Failed to change password', "error");
        return null;
    }
};

// User Management Endpoints
export const registerUserAPI = async (userInfo: UserRegister, showToast?: (msg: string, toastType?: 'error' | 'info') => void): Promise<void> => {
    const response = await fetch(getApiUrl(`/users/auth/register`), {
        method: 'POST',
        body: JSON.stringify(userInfo),
    });

    if (!response.ok) {
        if(showToast) showToast('Failed to register', "error");
        return;
    }

    const accessToken = response.headers.get('Authorization')?.replace('Bearer ', '');
    const refreshToken = response.headers.get('X-Refresh-Token');

    if (accessToken && refreshToken) {
        localStorage.setItem('accessToken', accessToken);
        localStorage.setItem('refreshToken', refreshToken);
    } else {
        if(showToast) showToast('Tokens are missing in the response', "error");
        return;
    }
};

export const loginUserAPI = async (userInfo: UserLogin, showToast?: (msg: string, toastType?: 'error' | 'info') => void): Promise<void> => {
    const response = await fetch(getApiUrl(`/users/auth/login`), {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-Client-Type': 'web'
        },
        body: JSON.stringify(userInfo),
    });

    if (!response.ok) {
        if(showToast) showToast('Failed to login', "error");
        return;
    }

    const accessToken = response.headers.get('Authorization')?.replace('Bearer ', '');
    const refreshToken = response.headers.get('X-Refresh-Token');

    if (accessToken && refreshToken) {
        localStorage.setItem('accessToken', accessToken);
        localStorage.setItem('refreshToken', refreshToken);
    } else {
        if(showToast) showToast('Tokens are missing in the response', "error");
        return;
    }
};

export const logoutUser = (): void => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
};

export const getUserAPI = async (showToast?: (msg: string, toastType?: 'error' | 'info') => void): Promise<UserInfo | null> => {
    const response = await fetchWithTokenRefresh(getApiUrl(`/users/`), {
        method: 'GET',
    });

    if (!response.ok) {
        if(showToast) showToast('Failed to fetch user', "error");
        return null;
    }

    const userInfo = await response.json();
    return userInfo;
};

export const updateUserAPI = async (userRequest: UserRequest, showToast?: (msg: string, toastType?: 'error' | 'info') => void): Promise<UserInfo | null> => {
    const response = await fetchWithTokenRefresh(getApiUrl(`/users/`), {
        method: 'PUT',
        body: JSON.stringify(userRequest),
    });

    if (!response.ok) {
        if(showToast) showToast('Failed to update user', "error");
        return null;
    }

    const updatedUserInfo = await response.json();
    return updatedUserInfo;
};

export const deleteUserAPI = async (showToast?: (msg: string, toastType?: 'error' | 'info') => void): Promise<void> => {
    const response = await fetchWithTokenRefresh(getApiUrl(`/users/`), {
        method: 'DELETE',
    });

    if (!response.ok) {
        if(showToast) showToast('Failed to delete user', "error");
        return;
    }
};

// File Management Endpoints
export const getFolderApi = async (folderId: string | null = null, currentContext:string, showToast?: (msg: string, toastType?: 'error' | 'info') => void): Promise<FolderItem | null> => {
    const response = await fetchWithTokenRefresh(getApiUrl(`/${currentContext}/${folderId ?? 'root'}`), {
        method: 'GET',
    });

    if (!response.ok) {
        if(showToast) showToast('Failed to fetch folder', "error");
        return null;
    }

    const data: FolderItem = await response.json();
    return data;
};

export const renameItemAPI = async (itemId: string, newName: string, showToast?: (msg: string, toastType?: 'error' | 'info') => void): Promise<void> => {
    const response = await fetchWithTokenRefresh(getApiUrl(`/files/${itemId}`), {
        method: 'PUT',
        body: JSON.stringify({ name: newName }),
    });

    if (!response.ok) {
        if(showToast) showToast('Failed to rename item', "error");
        return;
    }
};

export const moveItemAPI = async (itemId: string, newFolderId: string, showToast?: (msg: string, toastType?: 'error' | 'info') => void): Promise<void> => {
    const response = await fetchWithTokenRefresh(getApiUrl(`/files/${itemId}/move/${newFolderId}`), {
        method: 'PUT',
    });

    if (!response.ok) {
        if(showToast) showToast('Failed to move item', "error");
        return;
    }
};

export const deleteItemAPI = async (itemId: string, showToast?: (msg: string, toastType?: 'error' | 'info') => void): Promise<void> => {
    const response = await fetchWithTokenRefresh(getApiUrl(`/files/${itemId}`), {
        method: 'DELETE',
    });

    if (!response.ok) {
        if(showToast) showToast('Failed to delete item', "error");
        return;
    }
};

export const uploadFileAPI = async (file: File, targetFolderId: string, showToast?: (msg: string, toastType?: 'error' | 'info') => void): Promise<void> => {
    const formData = new FormData();
    formData.append('file', file);
    const response = await fetchWithTokenRefresh(getApiUrl(`/files/${targetFolderId}/upload`), {
        method: 'POST',
        body: formData,
    });

    if (!response.ok) {
        if(showToast) showToast('Failed to upload file', "error");
        return;
    }
};

export const downloadFileAPI = async (fileId: string, showToast?: (msg: string, toastType?: 'error' | 'info') => void): Promise<Blob | null> => {
    const response = await fetchWithTokenRefresh(getApiUrl(`/files/${fileId}/download`), {
        method: 'GET',
    });

    if (!response.ok) {
        if(showToast) showToast(`Failed to download file with ID: ${fileId}`, "error");
        return null;
    }

    const fileBlob = await response.blob();
    return fileBlob;
};

export const createFileAPI = async (request: CreateFileRequest, file: File, showToast?: (msg: string, toastType?: 'error' | 'info') => void): Promise<void> => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('request', JSON.stringify(request));

    const response = await fetchWithTokenRefresh(getApiUrl(`/files/`), {
        method: 'POST',
        body: formData,
    });

    if (!response.ok) {
        if(showToast) showToast('Failed to create file', "error");
        return;
    }
};

// Folder Management Endpoints
export const getRootFolderAPI = async (showToast?: (msg: string, toastType?: 'error' | 'info') => void): Promise<FolderItem | null> => {
    const response = await fetchWithTokenRefresh(getApiUrl(`/files/root`), {
        method: 'GET',
    });

    if (!response.ok) { 
        if(showToast) showToast('Failed to fetch root folder', "error");
        return null;
    }

    const data: FolderItem = await response.json();
    data.name = 'Pagina Principal'
    return data;
};

export const getFolderStructureAPI = async (showToast?: (msg: string, toastType?: 'error' | 'info') => void): Promise<FolderStructure | null> => {
    const response = await fetchWithTokenRefresh(getApiUrl(`/files/structure`), {
        method: 'GET',
    });

    if (!response.ok) {
        if(showToast) showToast('Failed to fetch folder structure', "error");
        return null;
    }

    const data: FolderStructure = await response.json();
    return data;
};

export const getFolderByIdAPI = async (folderId: string, showToast?: (msg: string, toastType?: 'error' | 'info') => void): Promise<FolderItem | null> => {
    const response = await fetchWithTokenRefresh(getApiUrl(`/files/${folderId}`), {
        method: 'GET',
    });

    if (!response.ok) {
        if(showToast) showToast('Failed to fetch folder by ID', "error");
        return null;
    }

    const data: FolderItem = await response.json();
    return data;
};

export const deleteFolderAPI = async (folderId: string, showToast?: (msg: string, toastType?: 'error' | 'info') => void): Promise<void> => {
    const response = await fetchWithTokenRefresh(getApiUrl(`/files/${folderId}`), {
        method: 'DELETE',
    });

    if (!response.ok) {
        if(showToast) showToast('Failed to delete folder', "error");
        return;
    }
};

// Shared Items Endpoints
export const getRootShareFolderAPI = async (showToast?: (msg: string, toastType?: 'error' | 'info') => void): Promise<FolderItem | null> => {
    const response = await fetchWithTokenRefresh(getApiUrl(`/share/root`), {
        method: 'GET',
    });

    if (!response.ok) {
        if(showToast) showToast('Failed to fetch shared root folder', "error");
        return null;
    }

    const folderInfo: FolderItem = await response.json();
    folderInfo.name = "Compartidos"
    return folderInfo;
};

export const shareItemAPI = async (shareRequest: ShareRequest, showToast?: (msg: string, toastType?: 'error' | 'info') => void): Promise<void> => {
    const response = await fetchWithTokenRefresh(getApiUrl(`/share/`), {
        method: 'POST',
        body: JSON.stringify(shareRequest),
    });

    if (!response.ok) {
        if(showToast) showToast('Failed to share item', "error");
        return;
    }
};

export const checkUsernameAPI = async (username: string, showToast?: (msg: string, toastType?: 'error' | 'info') => void) => {
    const response = await fetch(getApiUrl(`/users/auth/check/${username}`), {
        method: 'GET',
    });

    if (!response.ok) {
        if(showToast) showToast('Username not found', "error");
        return null;
    }

    return response.json();
};

export const getFileDetailsAPI = async (fileId:string, showToast?: (msg: string, toastType?: 'error' | 'info') => void) => {
    const response = await fetchWithTokenRefresh(getApiUrl(`/files/${fileId}`), {
        method: 'GET',
    });

    if (!response.ok) {
        if(showToast) showToast('Failed to fetch file details', "error");
        return null;
    }

    return response.json();
};

export const revokeShareAPI = async (fileId:string, showToast?: (msg: string, toastType?: 'error' | 'info') => void): Promise<void> => {
    const response = await fetchWithTokenRefresh(getApiUrl(`/share/${fileId}`), {
        method: 'DELETE',
    });

    if (!response.ok) {
        if(showToast) showToast('Failed to revoke share', "error");
        return;
    }
};

// Trash Endpoints
export const getRootTrashAPI = async (showToast?: (msg: string, toastType?: 'error' | 'info') => void): Promise<FolderItem | null> => {
    const response = await fetchWithTokenRefresh(getApiUrl(`/trash/root`), {
        method: 'GET',
    });

    if (!response.ok) {
        if(showToast) showToast('Failed to fetch trash root', "error");
        return null;
    }

    const data: FolderItem = await response.json();
    data.name = "Papelera"
    return data;
};

export const deleteTrashItemAPI = async (itemId: string, showToast?: (msg: string, toastType?: 'error' | 'info') => void): Promise<void> => {
    const response = await fetchWithTokenRefresh(getApiUrl(`/trash/${itemId}`), {
        method: 'DELETE',
    });

    if (!response.ok) {
        if(showToast) showToast('Failed to delete trash item', "error");
        return;
    }
};

export const restoreTrashItemAPI = async (itemId: string, showToast?: (msg: string, toastType?: 'error' | 'info') => void): Promise<void> => {
    const response = await fetchWithTokenRefresh(getApiUrl(`/trash/${itemId}/restore`), {
        method: 'PUT',
    });

    if (!response.ok) {
        if(showToast) showToast('Failed to restore trash item', "error");
        return;
    }
};

// History Endpoints
export const getHistoryAPI = async (userId: string, showToast?: (msg: string, toastType?: 'error' | 'info') => void): Promise<HistoryRecord[] | null> => {
    const response = await fetchWithTokenRefresh(getApiUrl(`/history/${userId}`), {
        method: 'GET',
    });

    if (!response.ok) {
        if(showToast) showToast('Failed to fetch history', "error");
        return null;
    }

    const data: HistoryRecord[] = await response.json();
    return data;
};

// Token Management Endpoints
export const refreshTokenAPI = async (showToast?: (msg: string, toastType?: 'error' | 'info') => void): Promise<void> => {
    const refreshToken = localStorage.getItem('refreshToken');

    if (!refreshToken) {
        if(showToast) showToast('No refresh token available', "error");
        return;
    }

    const response = await fetch(getApiUrl('/users/auth/keep-alive'), {
        method: 'POST',
        headers: {
            'X-Refresh-Token': refreshToken,
            'X-Client-Type': 'web'
        },
    });

    if (!response.ok) {
        if(showToast) showToast('Failed to refresh token', "error");  
        return;
    }

    const newAccessToken = response.headers.get('Authorization')?.replace('Bearer ', '');
    const newRefreshToken = response.headers.get('X-Refresh-Token');

    if (newAccessToken && newRefreshToken) {
        localStorage.setItem('accessToken', newAccessToken);
        localStorage.setItem('refreshToken', newRefreshToken);
    } else {
        if(showToast) showToast('Tokens are missing in the response', "error");
        return;
    }
};

export const checkUserAPI = async (showToast?: (msg: string, toastType?: 'error' | 'info') => void): Promise<boolean | null> => {
    const accessToken = localStorage.getItem('accessToken');

    if (!accessToken) {
        if(showToast) showToast('No access token available', "error");
        return null;
    }

    const response = await fetchWithTokenRefresh(getApiUrl(`/users/auth/check`), {
        method: 'POST',
    });

    if (!response.ok) {
        if(showToast) showToast('Failed to check user', "error");
        return null;
    }

    const userId = await response.json();
    return userId;
};

export const getWebSocketConnection = () => {
    const socket = new WebSocket(`ws://${API_CONFIG.host}:${API_CONFIG.port}/websocket`);
    return socket;
};
