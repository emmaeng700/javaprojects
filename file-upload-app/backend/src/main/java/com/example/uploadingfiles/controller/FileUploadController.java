package com.example.uploadingfiles.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.uploadingfiles.storage.StorageFileNotFoundException;
import com.example.uploadingfiles.storage.StorageService;

@Controller
@CrossOrigin(origins = "http://localhost:5173")
public class FileUploadController {

    private static final Logger log = LoggerFactory.getLogger(FileUploadController.class);
    private final StorageService storageService;

    @Autowired
    public FileUploadController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/")
    public String listUploadedFiles(Model model) {
        log.info("Listing uploaded files");
        
        List<String> files = storageService.loadAll()
                .map(path -> MvcUriComponentsBuilder.fromMethodName(
                        FileUploadController.class,
                        "serveFile",
                        path.getFileName().toString())
                        .build()
                        .toUri()
                        .toString())
                .collect(Collectors.toList());
        
        model.addAttribute("files", files);
        return "uploadForm";
    }

    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        log.info("Serving file: {}", filename);
        
        Resource file = storageService.loadAsResource(filename);

        if (file == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }

    @PostMapping("/")
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
                                    RedirectAttributes redirectAttributes) {
        
        log.info("Uploading file: {}", file.getOriginalFilename());
        
        storageService.store(file);
        redirectAttributes.addFlashAttribute("message",
                "You successfully uploaded " + file.getOriginalFilename() + "!");

        return "redirect:/";
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        log.error("File not found: {}", exc.getMessage());
        return ResponseEntity.notFound().build();
    }
}