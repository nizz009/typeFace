import React from 'react';
import { FileIcon, defaultStyles } from 'react-file-icon';
import './FileCard.css';

function FileCard({ file, onDownload, onDelete, onPreview, onToggleShareability, onGenerateShareLink}) {
  const getFileSize = (bytes) => {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  };

  const fileExtension = file.originalFileName.split('.').pop().toLowerCase();
  const iconStyle = defaultStyles[fileExtension] || {};

  return (
    <div className="file-card">
      <div className="file-icon-wrapper">
      <FileIcon
        extension={fileExtension}
          {...iconStyle}
        />
      </div>
      <h4 className="file-name">{file.originalFileName}</h4>
      <p className="file-size">{getFileSize(file.fileSize)}</p> 
      <p className="file-upload-date">Uploaded: {new Date(file.uploadedAt).toLocaleDateString()}</p>
      <div className="file-actions">
        <button className="preview-button" onClick={() => onPreview(file.fileName)}>Preview</button>
        <button className="button download-button" onClick={() => onDownload(file.fileName, file.originalFileName)}>Download</button>
        <button className="button delete-button" onClick={() => onDelete(file.fileName)}>Delete</button>
        <button
          type="button"
          className="toggle-share-button"
          onClick={() => onToggleShareability(file.fileName, file.shareable)}
        >
          {file.shareable ? 'Disable Share' : 'Enable Share'}
        </button>
        {file.shareable && (
          <button
            type="button"
            className="share-link-button"
            onClick={() => onGenerateShareLink(file.fileName)}
          >
            Get Share Link
          </button>
        )}
      </div>
    </div>
  );
}

export default FileCard;