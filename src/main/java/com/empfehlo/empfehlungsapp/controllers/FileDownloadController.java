package com.empfehlo.empfehlungsapp.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/files")
public class FileDownloadController {

    private static final Logger log = LoggerFactory.getLogger(FileDownloadController.class);

    @Value("${app.upload.dir}")
    private String uploadDir;

    @GetMapping("/download/{filename}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) {
        Path filePath = Paths.get(uploadDir).resolve(filename);

        if (Files.notExists(filePath)) {
            return ResponseEntity.notFound().build();
        }

        try {
            Resource resource = new UrlResource(filePath.toUri());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(Files.size(filePath))
                    .body(resource);
        } catch (MalformedURLException e) {
            log.error("Fehler beim Erstellen der Download-URL", e);
            return ResponseEntity.internalServerError().build();
        } catch (IOException e) {
            log.error("Fehler beim Abrufen der Datei", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}

