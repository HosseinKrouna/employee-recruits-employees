package com.empfehlo.empfehlungsapp.config;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class UploadInitializer {

    private String uploadDir;

    @PostConstruct
    public void init() {
        String projectDir = System.getProperty("user.dir");

        uploadDir = projectDir + File.separator + "uploads";

        File dir = new File(uploadDir);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (created) {
                System.out.println("📁 Upload-Verzeichnis angelegt: " + uploadDir);
            } else {
                System.err.println("❌ Upload-Verzeichnis konnte nicht erstellt werden!");
            }
        } else {
            System.out.println("✅ Upload-Verzeichnis existiert bereits: " + uploadDir);
        }

        System.setProperty("app.upload.dir", uploadDir);
    }

    public String getUploadDir() {
        return uploadDir;
    }
}

















