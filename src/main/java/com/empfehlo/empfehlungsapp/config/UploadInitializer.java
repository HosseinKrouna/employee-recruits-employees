package com.empfehlo.empfehlungsapp.config;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class UploadInitializer {

    private String uploadDir;

    @PostConstruct
    public void init() {
        // 📌 Holt das aktuelle Projektverzeichnis, egal wo es läuft
        String projectDir = System.getProperty("user.dir");

        // 📂 Legt den Zielordner "uploads" im Projektverzeichnis an
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

        // 🔁 Optional: Damit du in anderen Komponenten darauf zugreifen kannst
        System.setProperty("app.upload.dir", uploadDir);
    }

    public String getUploadDir() {
        return uploadDir;
    }
}

















//package com.empfehlo.empfehlungsapp.config;
//
//import jakarta.annotation.PostConstruct;
//import org.springframework.stereotype.Component;
//
//import java.io.File;
//
//@Component
//public class UploadDirectoryInitializer {
//
//    private String uploadDir;
//
//    @PostConstruct
//    public void createUploadDir() {
//        // Hole Benutzerverzeichnis
//        String userHome = System.getProperty("user.home");
//
//        // Lege Unterordner „empfehlo_uploads“ an
//        uploadDir = userHome + File.separator + "empfehlo_uploads";
//
//        File dir = new File(uploadDir);
//        if (!dir.exists()) {
//            boolean created = dir.mkdirs();
//            if (created) {
//                System.out.println("📂 Upload-Verzeichnis erstellt unter: " + uploadDir);
//            } else {
//                System.err.println("⚠️ Upload-Verzeichnis konnte nicht erstellt werden!");
//            }
//        } else {
//            System.out.println("✅ Upload-Verzeichnis existiert bereits: " + uploadDir);
//        }
//
//        // Optional: Du kannst den Pfad irgendwo global speichern oder loggen
//        System.setProperty("app.upload.dir", uploadDir); // Falls du es später brauchst
//    }
//
//    public String getUploadDir() {
//        return uploadDir;
//    }
//}
