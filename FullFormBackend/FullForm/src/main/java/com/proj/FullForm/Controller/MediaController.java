package com.proj.FullForm.Controller;

import com.proj.FullForm.Service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/media")
@RequiredArgsConstructor
public class MediaController {

    private final S3Service s3Service;

    @PostMapping("/upload")
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file,
                                         @RequestParam("type") String type) throws IOException {
        String key = s3Service.upload(file, type); // type = "audio", "video" or "images"
        return ResponseEntity.ok(key);
    }

    @GetMapping("/url")
    public ResponseEntity<String> url(@RequestParam("key") String key) {
        return ResponseEntity.ok(s3Service.getViewUrl(key));
    }
}
