
package com.dynamic.product.product.controller;

import com.dynamic.product.common.response.ApiResponse;
import com.dynamic.product.common.storage.FileStorageService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/file")
@SecurityRequirement(name = "bearerAuth")
public class FileController {

    private final FileStorageService fileStorageService;

    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> uploadFile(
            @RequestParam("file") MultipartFile file) {

        String fileUrl = fileStorageService.storeFile(file);

        ApiResponse<String> response = ApiResponse.success(
                HttpStatus.CREATED.value(),
                "File uploaded successfully",
                fileUrl
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{fileName}")
    public ResponseEntity<Resource> getFile(
            @PathVariable String fileName) {

        Resource resource = fileStorageService.loadFile(fileName);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        fileStorageService.getContentType(fileName)))
                .body(resource);
    }
}
