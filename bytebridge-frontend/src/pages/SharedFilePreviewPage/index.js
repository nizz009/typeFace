import React, { useState, useEffect, useCallback } from 'react';
import { useParams } from 'react-router-dom';
import * as fileService from '../../services/file';
import { getContentType } from '../../utils/commonUtils';
import './SharedFilePreviewPage.css';

function SharedFilePreviewPage() {
  const { fileName } = useParams();
  const [fileUrl, setFileUrl] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const fetchSharedFile = useCallback(async () => {
    setLoading(true);
    setError('');

    if (!fileName) {
      setError('File name not provided in the URL.');
      setLoading(false);
      return;
    }

    try {
      const response = await fileService.previewSharedFile(fileName);

      const fileExtension = fileName.split('.').pop().toLowerCase();
      const contentType = getContentType(fileExtension);

      const blob = new Blob([response.data], { type: contentType });
      const url = window.URL.createObjectURL(blob);

      setFileUrl(prevUrl => {
        if (prevUrl) {
          window.URL.revokeObjectURL(prevUrl);
        }
        return url;
      });

    } catch (err) {
      if (err.response && err.response.status === 403) {
        setError("You do not have access to the file");
      } else {
        setError(err.response?.data?.message || 'Failed to load shared file.');
      }
      console.error('Error fetching shared file:', err);
      setFileUrl(null);
    } finally {
      setLoading(false);
    }
  }, [fileName]);

  useEffect(() => {
    fetchSharedFile();

    return () => {};
  }, [fetchSharedFile]);

  if (loading) {
    return <div className="shared-preview-loading">Loading shared file...</div>;
  }

  if (error) {
    return <div className="shared-preview-error">Error: {error}</div>;
  }

  if (!fileUrl) {
    return <div className="shared-preview-message">No file to display or file still loading.</div>;
  }

  return (
    <div className="shared-preview-container">
      <h3>Previewing: {fileName}</h3>
      <iframe
        src={fileUrl}
        title={`Shared file: ${fileName}`}
        className="shared-file-iframe"
        frameBorder="0"
        allowFullScreen
      >
        Your browser does not support iframes.
      </iframe>
    </div>
  );
}

export default SharedFilePreviewPage;