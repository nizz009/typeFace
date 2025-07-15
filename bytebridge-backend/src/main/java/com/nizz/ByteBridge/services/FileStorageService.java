package com.nizz.ByteBridge.services;

import com.nizz.ByteBridge.dto.fileStorage.FileUploadResponse;
import com.nizz.ByteBridge.exceptions.FileAccessDeniedException;
import com.nizz.ByteBridge.exceptions.FileStorageException;
import com.nizz.ByteBridge.exceptions.InvalidFileException;
import com.nizz.ByteBridge.models.FileMetadata;
import com.nizz.ByteBridge.repositories.FileMetadataRepository;
import com.nizz.ByteBridge.repositories.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FileStorageService {
    @Value("${spring.servlet.multipart.max-file-size:300MB}")
    private DataSize maxFileSize;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Autowired
    private FileMetadataRepository fileMetadataRepository;

    @Autowired
    private UserRepository userRepository;

    private Path fileStorageLocation;

    @PostConstruct
    public void init() {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception e) {
            log.error("Error in creating base directory for file upload: ", e);
        }
    }

    public FileMetadata uploadFile(MultipartFile file, String userId) throws FileStorageException, InvalidFileException {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("File is empty or null");
        }

        if (file.getSize() > maxFileSize.toBytes()) {
            throw new InvalidFileException("File size exceeds maximum limit of " + maxFileSize.toMegabytes() + "MB");
        }

        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        if (fileName == null || fileName.isBlank()) {
            throw new InvalidFileException("Original filename is empty");
        }

        if (fileName.contains("..")) {
            throw new InvalidFileException("Invalid filename - contains path traversal characters");
        }

        Path userDir = this.fileStorageLocation.resolve(userId);
        try {
            Files.createDirectories(userDir);
        } catch (IOException e) {
            log.error("Error creating user directory for file upload: ", e);
            throw new FileStorageException("Could not create user directory");
        } catch (SecurityException e) {
            log.error("Security exception while creating user directory: ", e);
            throw new FileStorageException("Permission denied while creating user directory");
        }

        String fileExtension = "";
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            fileExtension = fileName.substring(dotIndex);
            fileName = fileName.substring(0, dotIndex);
        }

        String uniqueFileName = fileName + "_" + System.currentTimeMillis() + fileExtension;
        Path targetLocation = userDir.resolve(uniqueFileName);
        try {
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Error copying file to target location: {} : ", targetLocation, e);
            throw new FileStorageException("Failed to save file to storage");
        } catch (SecurityException e) {
            log.error("Security exception while copying file: ", e);
            throw new FileStorageException("Permission denied while saving file");
        }

        FileMetadata metadata = new FileMetadata();
        metadata.setFileName(uniqueFileName);
        metadata.setOriginalFileName(file.getOriginalFilename());
        metadata.setUserName(userId);
        metadata.setFileSize(file.getSize());
        metadata.setFileType(file.getContentType());
        metadata.setFilePath(targetLocation.toString());

        try {
            fileMetadataRepository.save(metadata);
        } catch (Exception e) {
            log.error("Error saving file metadata: ", e);
            // Delete uploaded file in case saving to file metadata fails
            try {
                Files.deleteIfExists(targetLocation);
            } catch (IOException ioException) {
                log.warn("Failed to clean up file after metadata save failure: ", ioException);
            }
            throw new FileStorageException("Failed to save file metadata");
        }

        return metadata;
    }

//    public List<FileUploadResponse> uploadMultipleFiles(List<MultipartFile> files, String userId) {
//        return files.stream()
//                .map(file -> {
//                    try {
//                        String fileName = uploadFile(file, userId);
//                        return new FileUploadResponse(fileName, "File Uploaded Successfully");
//                    } catch (IOException e) {
//                        return new FileUploadResponse(file.getOriginalFilename(), "Failed to upload file: " + e.getMessage());
//                    }
//                })
//                .collect(Collectors.toList());
//    }

    public Resource getFileContents(String fileName, String userId) throws InvalidFileException,
            FileStorageException, FileNotFoundException {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new InvalidFileException("Filename cannot be empty");
        }

        String cleanedFileName = StringUtils.cleanPath(fileName);
        if (cleanedFileName.contains("..") || cleanedFileName.contains("/") || cleanedFileName.contains("\\")) {
            throw new InvalidFileException("Invalid filename: contains path traversal characters");
        }

        FileMetadata fileMetadata = fileMetadataRepository.findByUserNameAndFileName(userId, fileName)
                .orElseThrow(() -> new FileNotFoundException("File not found or access denied: " + fileName));

        Path filePath = Paths.get(fileMetadata.getFilePath());
        if (!Files.exists(filePath)) {
            log.warn("File exists in database but not on filesystem: {}", filePath);
            throw new FileNotFoundException("File not found on storage: " + fileName);
        }

        Resource resource;
        try {
            resource = new UrlResource(filePath.toUri());
        } catch (MalformedURLException e) {
            log.error("Error creating resource for file: {}", filePath, e);
            throw new FileStorageException("Error accessing file");
        }

        if(resource.exists()) {
            return resource;
        } else {
            throw new FileNotFoundException("File not found " + fileName);
        }
    }

    public List<FileMetadata> fetchUserFiles(String userId) {
        return fileMetadataRepository.findByUserName(userId);
    }

    public FileMetadata fetchFileInfo(String fileName, String userId) throws FileNotFoundException {
        return fileMetadataRepository.findByUserNameAndFileName(userId, fileName)
                .orElseThrow(() -> new FileNotFoundException("File not found or access denied: " + fileName));
    }

    public void deleteFile(String fileName, String userId) throws FileStorageException, FileNotFoundException {
        try {
            FileMetadata fileMetadata = fileMetadataRepository.findByUserNameAndFileName(userId, fileName)
                    .orElseThrow(() -> new FileNotFoundException("File not found or access denied: " + fileName));

            Path filePath = Paths.get(fileMetadata.getFilePath());
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }

            fileMetadataRepository.delete(fileMetadata);
        } catch (Exception e) {
            throw new FileStorageException("Could not delete file " + fileName, e);
        }
    }

    public String determineContentType(String fileName) {
        try {
            Path path = Paths.get(fileName);
            String contentType = Files.probeContentType(path);
            return contentType != null ? contentType : MediaType.APPLICATION_OCTET_STREAM_VALUE;
        } catch (Exception e) {
            log.warn("Could not determine content type for file: {}", fileName);
            return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
    }

    public void updateFileShareability(String fileName, String userName, boolean shareability) throws FileStorageException,
            FileNotFoundException{
        try {
            FileMetadata fileMetadata = fileMetadataRepository.findByUserNameAndFileName(userName, fileName)
                    .orElseThrow(() -> new FileNotFoundException("File not found or access denied: " + fileName));

            fileMetadata.setShareable(shareability);
            fileMetadataRepository.save(fileMetadata);
        } catch (FileNotFoundException e) {
            log.error("File not found when updating shareability: {}", fileName, e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while updating shareability for file: {}", fileName, e);
            throw new FileStorageException("Could not update shareability for file " + fileName, e);
        }
    }

    public Resource getSharedFileContents(String fileName) throws FileNotFoundException, FileAccessDeniedException,
            FileStorageException {
        try {
            FileMetadata fileMetadata = fileMetadataRepository.findByFileName(fileName);

            if (fileMetadata == null) {
                log.warn("File metadata not found for file: {}", fileName);
                throw new FileNotFoundException("File not found: " + fileName);
            }

            if (!fileMetadata.isShareable()) {
                log.warn("Access denied - file is not shareable: {}", fileName);
                throw new FileAccessDeniedException("File is not shareable: " + fileName);
            }

            return getFileContents(fileName, fileMetadata.getUserName());

        } catch (FileNotFoundException | FileAccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while retrieving shared file: {}", fileName, e);
            throw new FileStorageException("Could not retrieve shared file " + fileName, e);
        }
    }
}
