import { API_CONFIG } from './config';
import { FolderItem, UserInfo, CreateFileRequest, HistoryRecord, ShareRequest, UserRequest, UserLogin, UserRegister, FolderStructure } from '../public/types';
import { useToast } from '../components/ToastProvider';

const showToast = useToast();

// Utility function to get the API URL
const getApiUrl = (endpoint: string): string => {
    const { host, port } = API_CONFIG;
    return port ? `http://${host}:${port}${endpoint}` : `http://${host}${endpoint}`;
};

// Utility function to handle token refresh
const fetchWithTokenRefresh = async (input: RequestInfo, init?: RequestInit): Promise<Response> => {
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
            await refreshTokenAPI();
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
            showToast('Failed to refresh token. Please log in again.', 'error');
            showToast('Token refresh failed');
        }
    }

    return response;
};


export const checkTokenAPI = async (): Promise<boolean> => {
    const accessToken = localStorage.getItem('accessToken');
    if (!accessToken) return false;

    const response = await fetchWithTokenRefresh(getApiUrl('/users/auth/check'), {
        method: 'POST',
    });

    if (!response.ok) {
        showToast('Failed to validate token.', 'error');
    }

    return response.ok;
};


export const getUserInfoAPI = async () => {
    const response = await fetchWithTokenRefresh(getApiUrl('/users'), {
        method: 'GET',
    });

    if (!response.ok) {
        showToast('Failed to fetch user info');
        return null;
    }

    return response.json();
};

export const updateUserInfoAPI = async (userInfo: UserInfo) => {
    const response = await fetchWithTokenRefresh(getApiUrl('/users'), {
        method: 'PUT',
        body: JSON.stringify(userInfo),
    });

    if (!response.ok) {
        showToast('Failed to update user info');
        return null;
    }

    return response.json();
};

export const changePasswordAPI = async (password: string) => { //TODO: añadir al servidor
    const response = await fetchWithTokenRefresh(getApiUrl('/users/auth/change-password'), {
        method: 'POST',
        body: JSON.stringify({ password }),
    });

    if (!response.ok) {
        showToast('Failed to change password');
        return null;
    }
};

// User Management Endpoints
export const registerUserAPI = async (userInfo: UserRegister): Promise<void> => {
    const response = await fetch(getApiUrl(`/users/auth/register`), {
        method: 'POST',
        body: JSON.stringify(userInfo),
    });

    if (!response.ok) {
        showToast('Failed to register');
        return;
    }

    const accessToken = response.headers.get('Authorization')?.replace('Bearer ', '');
    const refreshToken = response.headers.get('X-Refresh-Token');

    if (accessToken && refreshToken) {
        localStorage.setItem('accessToken', accessToken);
        localStorage.setItem('refreshToken', refreshToken);
    } else {
        showToast('Tokens are missing in the response');
        return;
    }
};

export const loginUserAPI = async (userInfo: UserLogin): Promise<void> => {
    const response = await fetch(getApiUrl(`/users/auth/login`), {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-Client-Type': 'web'
        },
        body: JSON.stringify(userInfo),
    });

    if (!response.ok) {
        showToast('Failed to login');
        return;
    }

    const accessToken = response.headers.get('Authorization')?.replace('Bearer ', '');
    const refreshToken = response.headers.get('X-Refresh-Token');

    if (accessToken && refreshToken) {
        localStorage.setItem('accessToken', accessToken);
        localStorage.setItem('refreshToken', refreshToken);
    } else {
        showToast('Tokens are missing in the response');
        return;
    }
};

export const logoutUser = (): void => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
};

export const getUserAPI = async (): Promise<UserInfo | null> => {
    const response = await fetchWithTokenRefresh(getApiUrl(`/users/`), {
        method: 'GET',
    });

    if (!response.ok) {
        showToast('Failed to fetch user');
        return null;
    }

    const userInfo = await response.json();
    return userInfo;
};

export const updateUserAPI = async (userRequest: UserRequest): Promise<UserInfo | null> => {
    const response = await fetchWithTokenRefresh(getApiUrl(`/users/`), {
        method: 'PUT',
        body: JSON.stringify(userRequest),
    });

    if (!response.ok) {
        showToast('Failed to update user');
        return null;
    }

    const updatedUserInfo = await response.json();
    return updatedUserInfo;
};

export const deleteUserAPI = async (): Promise<void> => {
    const response = await fetchWithTokenRefresh(getApiUrl(`/users/`), {
        method: 'DELETE',
    });

    if (!response.ok) {
        showToast('Failed to delete user');
        return;
    }
};

// File Management Endpoints
export const getFolderApi = async (folderId: string | null = null, currentContext:string): Promise<FolderItem | null> => {
    const response = await fetchWithTokenRefresh(getApiUrl(`/${currentContext}/${folderId ?? 'root'}`), {
        method: 'GET',
    });

    if (!response.ok) {
        showToast('Failed to fetch folder');
        return null;
    }

    const data: FolderItem = await response.json();
    return data;
};

export const renameItemAPI = async (itemId: string, newName: string): Promise<void> => {
    const response = await fetchWithTokenRefresh(getApiUrl(`/files/${itemId}`), {
        method: 'PUT',
        body: JSON.stringify({ name: newName }),
    });

    if (!response.ok) {
        showToast('Failed to rename item');
        return;
    }
};

export const moveItemAPI = async (itemId: string, newFolderId: string): Promise<void> => {
    const response = await fetchWithTokenRefresh(getApiUrl(`/files/${itemId}/move/${newFolderId}`), {
        method: 'PUT',
    });

    if (!response.ok) {
        showToast('Failed to move item');
        return;
    }
};

export const deleteItemAPI = async (itemId: string): Promise<void> => {
    const response = await fetchWithTokenRefresh(getApiUrl(`/files/${itemId}`), {
        method: 'DELETE',
    });

    if (!response.ok) {
        showToast('Failed to delete item');
        return;
    }
};

export const uploadFileAPI = async (file: File, targetFolderId: string): Promise<void> => {
    const formData = new FormData();
    formData.append('file', file);
    const response = await fetchWithTokenRefresh(getApiUrl(`/files/${targetFolderId}/upload`), {
        method: 'POST',
        body: formData,
    });

    if (!response.ok) {
        showToast('Failed to upload file');
        return;
    }
};

export const downloadFileAPI = async (fileId: string): Promise<Blob | null> => {
    const response = await fetchWithTokenRefresh(getApiUrl(`/files/${fileId}/download`), {
        method: 'GET',
    });

    if (!response.ok) {
        showToast(`Failed to download file with ID: ${fileId}`);
        return null;
    }

    const fileBlob = await response.blob();
    return fileBlob;
};

export const createFileAPI = async (request: CreateFileRequest, file: File): Promise<void> => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('request', JSON.stringify(request));

    const response = await fetchWithTokenRefresh(getApiUrl(`/files/`), {
        method: 'POST',
        body: formData,
    });

    if (!response.ok) {
        showToast('Failed to create file');
        return;
    }
};

// Folder Management Endpoints
export const getRootFolderAPI = async (): Promise<FolderItem | null> => {
    const response = await fetchWithTokenRefresh(getApiUrl(`/files/root`), {
        method: 'GET',
    });

    if (!response.ok) { 
        showToast('Failed to fetch root folder');
        return null;
    }

    const data: FolderItem = await response.json();
    data.name = 'Pagina Principal'
    return data;
};

export const getFolderStructureAPI = async (): Promise<FolderStructure | null> => {
    const response = await fetchWithTokenRefresh(getApiUrl(`/files/structure`), {
        method: 'GET',
    });

    if (!response.ok) {
        showToast('Failed to fetch folder structure');
        return null;
    }

    const data: FolderStructure = await response.json();
    return data;
};

export const getFolderByIdAPI = async (folderId: string): Promise<FolderItem | null> => {
    const response = await fetchWithTokenRefresh(getApiUrl(`/files/${folderId}`), {
        method: 'GET',
    });

    if (!response.ok) {
        showToast('Failed to fetch folder by ID');
        return null;
    }

    const data: FolderItem = await response.json();
    return data;
};

export const deleteFolderAPI = async (folderId: string): Promise<void> => {
    const response = await fetchWithTokenRefresh(getApiUrl(`/files/${folderId}`), {
        method: 'DELETE',
    });

    if (!response.ok) {
        showToast('Failed to delete folder');
        return;
    }
};

// Shared Items Endpoints
export const getRootShareFolderAPI = async (): Promise<FolderItem | null> => {
    const response = await fetchWithTokenRefresh(getApiUrl(`/share/root`), {
        method: 'GET',
    });

    if (!response.ok) {
        showToast('Failed to fetch shared root folder');
        return null;
    }

    const folderInfo: FolderItem = await response.json();
    folderInfo.name = "Compartidos"
    return folderInfo;
};

export const shareItemAPI = async (shareRequest: ShareRequest): Promise<void> => {
    const response = await fetchWithTokenRefresh(getApiUrl(`/share/`), {
        method: 'POST',
        body: JSON.stringify(shareRequest),
    });

    if (!response.ok) {
        showToast('Failed to share item');
        return;
    }
};

export const checkUsernameAPI = async (username: string) => {
    const response = await fetch(getApiUrl(`/users/auth/check/${username}`), {
        method: 'GET',
    });

    if (!response.ok) {
        showToast('Username not found');
        return null;
    }

    return response.json();
};

export const getFileDetailsAPI = async (fileId:string) => {
    const response = await fetchWithTokenRefresh(getApiUrl(`/files/${fileId}/details`), {
        method: 'GET',
    });

    if (!response.ok) {
        showToast('Failed to fetch file details');
        return null;
    }

    return response.json();
};

export const revokeShareAPI = async (shareRequest: ShareRequest): Promise<void> => {
    const response = await fetchWithTokenRefresh(getApiUrl(`/share/`), {
        method: 'DELETE',
        body: JSON.stringify(shareRequest),
    });

    if (!response.ok) {
        showToast('Failed to revoke share');
        return;
    }
};

// Trash Endpoints
export const getRootTrashAPI = async (): Promise<FolderItem | null> => {
    const response = await fetchWithTokenRefresh(getApiUrl(`/trash/root`), {
        method: 'GET',
    });

    if (!response.ok) {
        showToast('Failed to fetch trash root');
        return null;
    }

    const data: FolderItem = await response.json();
    data.name = "Papelera"
    return data;
};

export const deleteTrashItemAPI = async (itemId: string): Promise<void> => {
    const response = await fetchWithTokenRefresh(getApiUrl(`/trash/${itemId}`), {
        method: 'DELETE',
    });

    if (!response.ok) {
        showToast('Failed to delete trash item');
        return;
    }
};

export const restoreTrashItemAPI = async (itemId: string): Promise<void> => {
    const response = await fetchWithTokenRefresh(getApiUrl(`/trash/${itemId}/restore`), {
        method: 'PUT',
    });

    if (!response.ok) {
        showToast('Failed to restore trash item');
        return;
    }
};

// History Endpoints
export const getHistoryAPI = async (userId: string): Promise<HistoryRecord[] | null> => {
    const response = await fetchWithTokenRefresh(getApiUrl(`/history/${userId}`), {
        method: 'GET',
    });

    if (!response.ok) {
        showToast('Failed to fetch history');
        return null;
    }

    const data: HistoryRecord[] = await response.json();
    return data;
};

// Token Management Endpoints
export const refreshTokenAPI = async (): Promise<void> => {
    const refreshToken = localStorage.getItem('refreshToken');

    if (!refreshToken) {
        showToast('No refresh token available');
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
        showToast('Failed to refresh token');
        return;
    }

    const newAccessToken = response.headers.get('Authorization')?.replace('Bearer ', '');
    const newRefreshToken = response.headers.get('X-Refresh-Token');

    if (newAccessToken && newRefreshToken) {
        localStorage.setItem('accessToken', newAccessToken);
        localStorage.setItem('refreshToken', newRefreshToken);
    } else {
        showToast('Tokens are missing in the response');
        return;
    }
};

export const checkUserAPI = async (): Promise<boolean | null> => {
    const accessToken = localStorage.getItem('accessToken');

    if (!accessToken) {
        showToast('No access token available');
        return null;
    }

    const response = await fetchWithTokenRefresh(getApiUrl(`/users/auth/check`), {
        method: 'POST',
    });

    if (!response.ok) {
        showToast('Failed to check user');
        return null;
    }

    const userId = await response.json();
    return userId;
};

export const getWebSocketConnection = () => {
    const socket = new WebSocket(`ws://${API_CONFIG.host}:${API_CONFIG.port}/websocket`);
    return socket;
};
