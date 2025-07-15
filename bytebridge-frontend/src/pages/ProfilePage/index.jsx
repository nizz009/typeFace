import React, {
  useState,
  useEffect,
  useRef,
  useCallback,
  useMemo,
} from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../contexts/AuthContext";
import * as fileService from "../../services/file";
import { getContentType } from "../../utils/commonUtils";
import FileCard from "../../components/FileCard/FileCard";
import "./ProfilePage.css";

function UserProfilePage() {
  const { user, isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();
  const [files, setFiles] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [selectedFile, setSelectedFile] = useState(null);
  const [searchQuery, setSearchQuery] = useState("");
  const [fileTypeFilter, setFileTypeFilter] = useState("all");
  const fileInputRef = useRef(null);

  useEffect(() => {
    if (!isAuthenticated) {
      navigate("/login");
    }
  }, [isAuthenticated, navigate]);

  const fetchFiles = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      const response = await fileService.getFiles();
      setFiles(response.data);
    } catch (err) {
      setError(err.response?.data?.message || "Failed to fetch files.");
      console.error("Error fetching files:", err);
      if (err.response && err.response.status === 401) {
        logout();
        navigate("/login");
      }
    } finally {
      setLoading(false);
    }
  }, [logout, navigate]);

  useEffect(() => {
    if (isAuthenticated) {
      fetchFiles();
    }
  }, [isAuthenticated, fetchFiles]);

  const handleFileUpload = async (e) => {
    e.preventDefault();
    if (!selectedFile) {
      setError("Please select a file to upload.");
      return;
    }
    setLoading(true);
    setError("");
    const formData = new FormData();
    formData.append("file", selectedFile);
    try {
      let response = await fileService.uploadFile(formData);

      setFiles((prevFiles) => [...prevFiles, response.data.fileMetadata]);
      setSelectedFile(null);
      if (fileInputRef.current) {
        fileInputRef.current.value = "";
      }
      setError("");
    } catch (err) {
      setError(err.response?.data?.message || "File upload failed.");
      console.error("Error uploading file:", err);
    } finally {
      setLoading(false);
    }
  };

  const handleFileDownload = async (fileId, fileName) => {
    try {
      const response = await fileService.downloadFile(fileId);
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement("a");
      link.href = url;
      link.setAttribute("download", fileName);
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    } catch (err) {
      setError(err.response?.data?.message || "File download failed.");
      console.error("Error downloading file:", err);
    }
  };

  const handleFileDelete = async (fileId) => {
    if (window.confirm("Are you sure you want to delete this file?")) {
      setLoading(true);
      setError("");
      try {
        await fileService.deleteFile(fileId);

        setFiles((prevFiles) => {
          const updatedFiles = prevFiles.filter((file) => file.fileName !== fileId);
          return updatedFiles;
        });
        setError("");
      } catch (err) {
        const errorMessage =
          err.response?.data?.message || "File deletion failed.";
        setError(errorMessage);
        console.error(
          `[handleFileDelete] Error deleting file ID ${fileId}:`,
          errorMessage,
          err
        );
      } finally {
        setLoading(false);
      }
    }
  };

  const handleFilePreview = async (fileId) => {
    try {
      const response = await fileService.previewFile(fileId);
      const fileExtension = fileId.split(".").pop().toLowerCase();
      let contentType = getContentType(fileExtension);
      const blob = new Blob([response.data], { type: contentType });
      const url = window.URL.createObjectURL(blob);
      window.open(url, "_blank");
      window.URL.revokeObjectURL(url);
    } catch (err) {
      setError(err.response?.data?.message || "File preview failed.");
      console.error("Error previewing file:", err);
    }
  };

  const handleToggleShareability = async (fileName, currentShareability) => {
    setLoading(true);
    try {
      let payload = {
        shareable: !currentShareability,
      };
      await fileService.toggleFileShareability(fileName, payload);

      setFiles((prevFiles) =>
        prevFiles.map((file) =>
          file.fileName === fileName
            ? { ...file, shareable: !currentShareability }
            : file
        )
      );
      setError(""); 
    } catch (err) {
      setError(err.response?.data?.message || "Failed to toggle shareability.");
      console.error("Error toggling shareability:", err);
    } finally {
      setLoading(false);
    }
  };

  const handleGenerateShareLink = async (fileName) => {
    try {
      const shareLink = `${
        window.location.origin
      }/shared-preview/${encodeURIComponent(fileName)}`;
      const textArea = document.createElement("textarea");
      textArea.value = shareLink;
      document.body.appendChild(textArea);
      textArea.select();
      try {
        document.execCommand("copy");
        alert("Share link copied to clipboard: " + shareLink);
      } catch (err) {
        console.error("Failed to copy text: ", err);
        alert("Failed to copy link. Please copy manually: " + shareLink);
      }
      document.body.removeChild(textArea);
      setError("");
    } catch (err) {
      setError("Failed to generate share link for clipboard.");
      console.error("Error generating share link for clipboard:", err);
    }
  };

  const uniqueFileTypes = useMemo(() => {
    const types = new Set();
    files.forEach((file) => {
      const parts = file.originalFileName.split(".");
      if (parts.length > 1) {
        types.add(parts.pop().toLowerCase());
      } else {
        types.add("other");
      }
    });
    return ["all", ...Array.from(types).sort()];
  }, [files]);

  const filteredFiles = useMemo(() => {
    let result = files;
    if (fileTypeFilter !== "all") {
      result = result.filter((file) => {
        const parts = file.originalFileName.split(".");
        const fileExtension =
          parts.length > 1 ? parts.pop().toLowerCase() : "other";
        return fileExtension === fileTypeFilter;
      });
    }
    if (searchQuery) {
      result = result.filter((file) =>
        file.originalFileName.toLowerCase().includes(searchQuery.toLowerCase())
      );
    }
    return result;
  }, [files, fileTypeFilter, searchQuery]);

  if (!isAuthenticated) {
    return <div className="profile-loading">Redirecting to login...</div>;
  }

  return (
    <div className="user-profile-container">
      <h2>Welcome, {user?.firstName || "User"}!</h2>
      <div className="upload-section">
        <h3>Upload New File</h3>
        <form onSubmit={handleFileUpload}>
          <input
            type="file"
            id="file-upload-input"
            ref={fileInputRef}
            onChange={(e) => setSelectedFile(e.target.files[0])}
            required
          />
          <button type="submit" className="upload-button" disabled={loading}>
            {loading ? "Processing..." : "Upload File"}
          </button>
        </form>
        {error && <p className="error-message">{error}</p>}
      </div>
      <div className="file-list-section">
        <h3>Your Files</h3>
        <div className="filter-controls">
          <input
            type="text"
            placeholder="Search files..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="file-search-input"
          />
          <select
            value={fileTypeFilter}
            onChange={(e) => setFileTypeFilter(e.target.value)}
            className="file-type-filter-select"
          >
            {uniqueFileTypes.map((type) => (
              <option key={type} value={type}>
                {type === "all" ? "All Types" : type.toUpperCase()}
              </option>
            ))}
          </select>
        </div>
        {loading && <p className="file-list-loading">Loading your files...</p>}
        {!loading &&
        filteredFiles.length === 0 &&
        (searchQuery !== "" || fileTypeFilter !== "all") ? (
          <p>No files found matching your search or filter criteria.</p>
        ) : !loading && filteredFiles.length === 0 ? (
          <p>You haven't uploaded any files yet. Upload one to get started!</p>
        ) : (
          <div className="file-grid">
            {filteredFiles.map((file) => (
              <FileCard
                key={file.id}
                file={file}
                onDownload={handleFileDownload}
                onDelete={handleFileDelete}
                onPreview={handleFilePreview}
                onToggleShareability={handleToggleShareability}
                onGenerateShareLink={() =>
                  handleGenerateShareLink(file.fileName)
                }
              />
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

export default UserProfilePage;