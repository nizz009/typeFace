package com.nizz.ByteBridge.repositories;

import com.nizz.ByteBridge.models.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, String> {
    List<FileMetadata> findByUserName(String userName);
    FileMetadata findByFileName(String fileName);
    Optional<FileMetadata> findByUserNameAndFileName(String userName, String fileName);
}
