package com.nizz.ByteBridge.controllers;

import com.nizz.ByteBridge.dto.fileStorage.FileUploadResponse;
import com.nizz.ByteBridge.exceptions.FileAccessDeniedException;
import com.nizz.ByteBridge.exceptions.FileStorageException;
import com.nizz.ByteBridge.exceptions.InvalidFileException;
import com.nizz.ByteBridge.models.FileMetadata;
import com.nizz.ByteBridge.services.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/files")
public class FileStorageController {
    @Autowired
    private FileStorageService fileStorageService;

    @PostMapping("/upload")
    public ResponseEntity<FileUploadResponse> uploadFile(@RequestParam("file") MultipartFile file, Authentication authentication) {
        try {
            String userId = authentication.getName();
            FileMetadata fileMetadata = fileStorageService.uploadFile(file, userId);
            FileUploadResponse response = new FileUploadResponse(fileMetadata, "File uploaded successfully");
            return ResponseEntity.ok(response);
        } catch (InvalidFileException e) {
            log.warn("Invalid file upload attempt: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new FileUploadResponse(null, e.getMessage()));
        } catch (FileStorageException e) {
            log.error("File storage error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new FileUploadResponse(null, e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during file upload: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new FileUploadResponse(null, "An unexpected error occurred during file upload"));
        }
    }

//    @PostMapping("/upload-multiple")
//    public ResponseEntity<List<FileUploadResponse>> uploadMultipleFiles(@RequestParam("files") List<MultipartFile> files, Authentication authentication) {
//        String userId = authentication.getName();
//        List<FileUploadResponse> responses = fileStorageService.uploadMultipleFiles(files, userId);
//        return ResponseEntity.ok(responses);
//    }

    @GetMapping("/download/{fileName}")
    public ResponseEntity<?> downloadFile(@PathVariable String fileName,Authentication authentication) {
        try {
            String userId = authentication.getName();
            Resource resource = fileStorageService.getFileContents(fileName, userId);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (InvalidFileException e) {
            log.warn("Invalid file download request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body("Invalid request: " + e.getMessage());
        } catch (FileNotFoundException e) {
            log.warn("File not found during download: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (FileStorageException e) {
            log.error("Storage error during file download: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Storage error: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during file download for file '{}': ", fileName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred during file download");
        }
    }

    @GetMapping
    public ResponseEntity<List<FileMetadata>> fetchUserFiles(Authentication authentication) {
        String userId = authentication.getName();
        List<FileMetadata> userFiles = fileStorageService.fetchUserFiles(userId);
        return ResponseEntity.ok(userFiles);
    }

    @GetMapping("/file-info/{fileName}")
    public ResponseEntity<FileMetadata> fetchFileInfo(@PathVariable String fileName, Authentication authentication) {
        try {
            String userId = authentication.getName();
            FileMetadata fileInfo = fileStorageService.fetchFileInfo(fileName, userId);

            return ResponseEntity.ok(fileInfo);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/delete/{fileName}")
    public ResponseEntity<String> deleteFile(@PathVariable String fileName, Authentication authentication) {
        try {
            String userId = authentication.getName();
            fileStorageService.deleteFile(fileName, userId);
            return ResponseEntity.ok("File is successfully deleted");
        } catch (FileNotFoundException e) {
            log.warn("File not found during deletion: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (FileStorageException e) {
            log.error("Storage error during file deletion: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Storage error: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during file deletion for file '{}': ", fileName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred during file deletion");
        }
    }

    @GetMapping("/preview/{fileName}")
    public ResponseEntity<Resource> previewFile(@PathVariable String fileName, Authentication authentication) {
        try {
            String userId = authentication.getName();
            Resource resource = fileStorageService.getFileContents(fileName, userId);

            String contentType = fileStorageService.determineContentType(resource.getFilename());

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (FileNotFoundException e) {
            log.error("File not found for preview: {}", fileName, e);
            return ResponseEntity.notFound().build();
        } catch (FileStorageException e) {
            log.error("File storage error during preview: {}", fileName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (InvalidMediaTypeException e) {
            log.error("Invalid media type for file preview: {}", fileName, e);
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).build();
        } catch (Exception e) {
            log.error("Unexpected error during file preview '{}': {}", fileName, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/update-shareability/{fileName}")
    public ResponseEntity<String> updateFileShareability(@PathVariable String fileName,
                                                         @RequestBody Map<String, Boolean> request,
                                                         Authentication authentication) {
        try {
            String userId = authentication.getName();
            Boolean isShareable = request.get("shareable");

            if (isShareable == null) {
                return ResponseEntity.badRequest().body("Shareable field is required");
            }

            fileStorageService.updateFileShareability(fileName, userId, isShareable);

            String message = isShareable ? "File is now shareable" : "File sharing disabled";
            return ResponseEntity.ok(message);

        } catch (FileNotFoundException e) {
            log.error("File not found for updating shareability: {}", fileName, e);
            return ResponseEntity.notFound().build();
        } catch (FileStorageException e) {
            log.error("File storage error during updating shareability: {}", fileName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            log.error("Unexpected error during file shareability '{}': {}", fileName, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/share/preview/{fileName}")
    public ResponseEntity<Resource> sharePreviewFile(@PathVariable String fileName) {
        try {
            Resource resource = fileStorageService.getSharedFileContents(fileName);

            String contentType = fileStorageService.determineContentType(resource.getFilename());

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (FileNotFoundException e) {
            log.error("Shared file not found or not shareable: {}", fileName, e);
            return ResponseEntity.notFound().build();
        } catch (FileStorageException e) {
            log.error("File storage error during shared file preview: {}", fileName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (FileAccessDeniedException e) {
            log.error("File Access Denial error during shared file preview: {}", fileName, e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (InvalidMediaTypeException e) {
            log.error("Invalid media type for shared file preview: {}", fileName, e);
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).build();
        } catch (Exception e) {
            log.error("Unexpected error during shared file preview '{}': {}", fileName, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
