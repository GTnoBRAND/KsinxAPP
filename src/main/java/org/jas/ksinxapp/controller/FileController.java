package org.jas.ksinxapp.controller;

import lombok.RequiredArgsConstructor;
import org.jas.ksinxapp.service.MinIoStorageService;
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
@RequiredArgsConstructor
public class FileController {

    private final MinIoStorageService minIoStorageService;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam MultipartFile file) {

        String fileDownloadUri = minIoStorageService.publicUpload(file);

        //return a simple json object containing a enw url
        Map<String, String> response = new HashMap<>();
        response.put("fileDownloadUri", fileDownloadUri);

        return ResponseEntity.ok(response);
    }
}
