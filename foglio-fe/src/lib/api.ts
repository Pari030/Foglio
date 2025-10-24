import axios from 'axios';

const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

const api = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add API key to requests if available
api.interceptors.request.use((config) => {
  if (typeof window !== 'undefined') {
    const apiKey = localStorage.getItem('apiKey');
    if (apiKey) {
      config.headers['X-API-KEY'] = apiKey;
    }
  }
  return config;
});

export interface UserDTO {
  id: number;
  name: string;
  apiKey: string;
}

export interface FileMetadataDTO {
  id: string;
  originalName: string;
  extension: string;
  contentType: string;
  size: number;
  ownerId: number;
  isPublic: boolean;
  storedFileName: string;
  createdAt: string;
  requestCount: number;
  lastRequestedAt: string | null;
}

export const authAPI = {
  register: async (name: string): Promise<UserDTO> => {
    const response = await api.post(`/users/register?name=${encodeURIComponent(name)}`);
    return response.data;
  },

  getMe: async (): Promise<UserDTO> => {
    const response = await api.get('/users/me');
    return response.data;
  },
};

export const fileAPI = {
  listMine: async (): Promise<FileMetadataDTO[]> => {
    const response = await api.get('/files');
    return response.data;
  },
  upload: async (file: File, isPublic: boolean = false): Promise<FileMetadataDTO> => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('public', String(isPublic));

    const response = await api.post('/files/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },

  getMetadata: async (fileId: string): Promise<FileMetadataDTO> => {
    const response = await api.get(`/files/${fileId}/metadata`);
    return response.data;
  },

  getPreviewUrl: (fileId: string, isPublic: boolean = false): string => {
    if (isPublic) {
      return `${API_URL}/files/${fileId}/preview`;
    }
    const apiKey = typeof window !== 'undefined' ? localStorage.getItem('apiKey') : '';
    return `${API_URL}/files/${fileId}/preview${apiKey ? `?apiKey=${apiKey}` : ''}`;
  },

  getDownloadUrl: (fileId: string, isPublic: boolean = false): string => {
    if (isPublic) {
      return `${API_URL}/files/${fileId}/download`;
    }
    const apiKey = typeof window !== 'undefined' ? localStorage.getItem('apiKey') : '';
    return `${API_URL}/files/${fileId}/download${apiKey ? `?apiKey=${apiKey}` : ''}`;
  },
};

export default api;
