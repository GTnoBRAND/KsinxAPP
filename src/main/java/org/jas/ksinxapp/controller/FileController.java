package org.jas.ksinxapp.controller;

import org.jas.ksinxapp.service.FileStorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/files")
public class FileController {

    private final FileStorageService service;

    public FileController(FileStorageService service) {
        this.service = service;
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam MultipartFile file) {

        String fileDownloadUri = service.storeFile(file);

        //return a simple json object containing a enw url
        Map<String, String> response = new HashMap<>();
        response.put("fileDownloadUri", fileDownloadUri);

        return ResponseEntity.ok(response);
    }
}
