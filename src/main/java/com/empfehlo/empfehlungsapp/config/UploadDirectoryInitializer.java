package com.empfehlo.empfehlungsapp.config;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class UploadDirectoryInitializer {

    private String uploadDir;

    @PostConstruct
    public void createUploadDir() {
        String userHome = System.getProperty("user.home");

        uploadDir = userHome + File.separator + "empfehlo_uploads";

        File dir = new File(uploadDir);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (created) {
                System.out.println("📂 Upload-Verzeichnis erstellt unter: " + uploadDir);
            } else {
                System.err.println("⚠️ Upload-Verzeichnis konnte nicht erstellt werden!");
            }
        } else {
            System.out.println("✅ Upload-Verzeichnis existiert bereits: " + uploadDir);
        }

    }

    public String getUploadDir() {
        return uploadDir;
    }
}
