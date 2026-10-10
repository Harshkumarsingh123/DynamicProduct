package com.dynamic.product.common.storage;

import com.dynamic.product.exception.CustomException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path uploadPath;

    public FileStorageService(@Value("${file.upload-dir}") String uploadDir) {

        this.uploadPath = Paths.get(uploadDir)
                .toAbsolutePath()
                .normalize();

        try {
            Files.createDirectories(uploadPath);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Could not create upload directory",
                    e
            );
        }
    }

    public String storeFile(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new CustomException(
                    "File cannot be empty",
                    HttpStatus.BAD_REQUEST
            );
        }

        String originalFilename =
                file.getOriginalFilename();

        String extension = "";

        if (originalFilename != null &&
                originalFilename.contains(".")) {

            extension =
                    originalFilename.substring(
                            originalFilename.lastIndexOf(".")
                    ).toLowerCase();
        }

        String fileName =
                UUID.randomUUID() + extension;

        try {

            Path targetLocation =
                    uploadPath.resolve(fileName)
                            .normalize();

            // Security check
            if (!targetLocation.startsWith(uploadPath)) {
                throw new CustomException(
                        "Invalid file path",
                        HttpStatus.BAD_REQUEST
                );
            }

            file.transferTo(targetLocation);

            return "/api/file/" + fileName;

        } catch (IOException e) {

            throw new CustomException(
                    "Could not store file",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    public Resource loadFile(String fileName) {

        try {

            Path filePath =
                    uploadPath.resolve(fileName)
                            .normalize();

            if (!filePath.startsWith(uploadPath)) {
                throw new CustomException(
                        "Invalid file path",
                        HttpStatus.BAD_REQUEST
                );
            }

            Resource resource =
                    new UrlResource(
                            filePath.toUri()
                    );

            if (!resource.exists() ||
                    !resource.isReadable()) {

                throw new CustomException(
                        "File not found",
                        HttpStatus.NOT_FOUND
                );
            }

            return resource;

        } catch (IOException e) {

            throw new CustomException(
                    "Could not read file",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    public String getContentType(String fileName) {

        try {

            Path filePath =
                    uploadPath.resolve(fileName)
                            .normalize();

            if (!filePath.startsWith(uploadPath)) {
                throw new CustomException(
                        "Invalid file path",
                        HttpStatus.BAD_REQUEST
                );
            }

            String contentType =
                    Files.probeContentType(filePath);

            if (contentType == null) {
                return "application/octet-stream";
            }

            return contentType;

        } catch (IOException e) {

            return "application/octet-stream";
        }
    }
}