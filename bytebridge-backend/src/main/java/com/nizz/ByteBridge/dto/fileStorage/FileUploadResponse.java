package com.nizz.ByteBridge.dto.fileStorage;

import com.nizz.ByteBridge.models.FileMetadata;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FileUploadResponse {
    FileMetadata fileMetadata;
    String message;
}
