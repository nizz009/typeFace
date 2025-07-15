import api from './api';

export const getFiles = () => {
  return api.get('/files');
};

export const uploadFile = (formData) => {
  return api.post('/files/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
};

export const downloadFile = (fileId) => {
  return api.get(`/files/download/${fileId}`, { responseType: 'blob' });
};

export const deleteFile = (fileId) => {
  return api.delete(`/files/delete/${fileId}`);
};

export const previewFile = (fileName) => api.get(`/files/preview/${fileName}`, {
  responseType: 'blob',
});

export const toggleFileShareability = (fileName, payload) =>
  api.put(`/files/update-shareability/${fileName}`, payload);

export const previewSharedFile = (fileName) => api.get(`/files/share/preview/${fileName}`, {
  responseType: 'blob',
});
