package com.empfehlo.empfehlungsapp.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class UploadInitializer {

    private static final Logger log = LoggerFactory.getLogger(UploadInitializer.class);
    private Path uploadDir;

    @PostConstruct
    public void init() {
        uploadDir = Paths.get(System.getProperty("user.dir"), "uploads");

        try {
            if (Files.notExists(uploadDir)) {
                Files.createDirectories(uploadDir);
                log.info("📁 Upload-Verzeichnis angelegt: {}", uploadDir);
            } else {
                log.info("✅ Upload-Verzeichnis existiert bereits: {}", uploadDir);
            }
        } catch (IOException e) {
            log.error("❌ Upload-Verzeichnis konnte nicht erstellt werden!", e);
        }

        System.setProperty("app.upload.dir", uploadDir.toString());
    }

    public Path getUploadDir() {
        return uploadDir;
    }
}