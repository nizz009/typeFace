package com.nizz.ByteBridge.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "fileMetadata")
@Data
public class FileMetadata {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String userName;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String originalFileName;

    @Column(nullable = false)
    private String filePath;

    @Column(nullable = false)
    private String fileType;

    @Column
    private boolean shareable = false;

    @Column(length = 500)
    private String description;

    @Column(length = 1000)
    private String tags;

    @CreationTimestamp
    @Column(updatable = false)
    private String uploadedAt;

    @UpdateTimestamp
    private String modifiedAt;
}
