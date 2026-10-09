package com.dynamic.product.controller;

import com.dynamic.product.dto.response.ApiResponse;
import com.dynamic.product.service.FileStorageService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/file")
@SecurityRequirement(name = "bearerAuth")
public class FileController {

    private final FileStorageService fileStorageService;

    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse> uploadFile(
            @RequestParam("file") MultipartFile file) {

        String fileUrl =
                fileStorageService.storeFile(file);

        ApiResponse apiResponse = new ApiResponse(
                HttpStatus.CREATED.value(),
                "File uploaded successfully: " + fileUrl,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(
                apiResponse,
                HttpStatus.CREATED
        );
    }

    @GetMapping("/{fileName}")
    public ResponseEntity<Resource> getFile(
            @PathVariable String fileName) {

        Resource resource =
                fileStorageService.loadFile(fileName);

        return ResponseEntity.ok().contentType(MediaType.parseMediaType(
                fileStorageService.getContentType(fileName)))
                .body(resource);
    }
}