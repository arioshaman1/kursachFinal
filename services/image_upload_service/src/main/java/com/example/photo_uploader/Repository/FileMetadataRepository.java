package com.example.photo_uploader.Repository;

import com.example.photo_uploader.entities.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {
    List<FileMetadata> findByUsername(String username);
    Optional<FileMetadata> findByFileName(String fileName);
}