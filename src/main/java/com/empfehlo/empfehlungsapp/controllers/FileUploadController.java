package com.empfehlo.empfehlungsapp.controllers;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;



@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    @PostConstruct
    public void logInit() {
        log.info("✅ FileUploadController wurde korrekt geladen!");
    }


    private static final Logger log = LoggerFactory.getLogger(FileUploadController.class);

    @Value("${app.upload.dir}")
    private String uploadDir;

    @PostMapping("/upload")
    public ResponseEntity<String> handleFileUpload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty() || !isValidPdf(file)) {
            return ResponseEntity.badRequest().body("❌ Nur nicht-leere PDF-Dateien erlaubt!");
        }

        try {
            String uniqueFilename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path targetPath = Paths.get(uploadDir).resolve(uniqueFilename);
            log.info("🛠 Speicherort: {}", targetPath.toAbsolutePath());

            Files.createDirectories(targetPath.getParent());
            log.info("📝 Upload-Zielpfad: {}", targetPath.toAbsolutePath());
            log.info("📂 Zielpfad für Upload: " + targetPath.toAbsolutePath());

            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            return ResponseEntity.ok(uniqueFilename);
        } catch (IOException e) {
            log.error("Fehler beim Hochladen der Datei", e);
            return ResponseEntity.internalServerError().body("❌ Fehler beim Hochladen");
        }
    }

    private boolean isValidPdf(MultipartFile file) {
        String filename = file.getOriginalFilename();
        String contentType = file.getContentType();
        return filename != null && filename.toLowerCase().endsWith(".pdf")
                && contentType != null && contentType.equalsIgnoreCase("application/pdf");
    }
}
