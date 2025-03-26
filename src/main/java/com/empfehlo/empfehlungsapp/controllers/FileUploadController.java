package com.empfehlo.empfehlungsapp.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    @Value("${app.upload.dir}")
    private String uploadDir;

    @PostMapping("/upload")
    public ResponseEntity<String> handleFileUpload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("⚠ Datei ist leer");
        }

        String filename = file.getOriginalFilename();
        String contentType = file.getContentType();

        if (filename == null || !filename.toLowerCase().endsWith(".pdf") ||
                contentType == null || !contentType.equalsIgnoreCase("application/pdf")) {
            return ResponseEntity.badRequest().body("❌ Nur PDF-Dateien erlaubt!");
        }

        try {
            String uniqueFilename = System.currentTimeMillis() + "_" + filename;
            File targetFile = new File(uploadDir + File.separator + uniqueFilename);
            file.transferTo(targetFile);

            return ResponseEntity.ok(uniqueFilename);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("❌ Fehler beim Hochladen");
        }
    }

    @GetMapping("/download/{filename}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) throws IOException {
        File file = new File(uploadDir + File.separator + filename);
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(file.length())
                .body(resource);
    }

}
