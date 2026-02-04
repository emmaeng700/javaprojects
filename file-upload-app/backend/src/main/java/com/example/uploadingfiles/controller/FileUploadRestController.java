package com.example.uploadingfiles.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.uploadingfiles.exception.StorageFileNotFoundException;
import com.example.uploadingfiles.service.StorageService;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class FileUploadRestController {

    private static final Logger log = LoggerFactory.getLogger(FileUploadRestController.class);
    private final StorageService storageService;

    @Autowired
    public FileUploadRestController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/files")
    public ResponseEntity<List<String>> listFiles() {
        log.info("API: Listing all files");
        
        List<String> files = storageService.loadAll()
                .map(path -> path.getFileName().toString())
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(files);
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> handleFileUpload(
            @RequestParam("file") MultipartFile file) {
        
        log.info("API: Uploading file: {}", file.getOriginalFilename());
        
        try {
            storageService.store(file);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "File uploaded successfully: " + file.getOriginalFilename());
            response.put("filename", file.getOriginalFilename());
            response.put("size", String.valueOf(file.getSize()));
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to upload file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/download/{filename:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) {
        log.info("API: Downloading file: {}", filename);
        
        try {
            Resource file = storageService.loadAsResource(filename);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + file.getFilename() + "\"")
                    .body(file);
        } catch (StorageFileNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getFileCount() {
        long count = storageService.loadAll().count();
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }
}