package com.proj.FullForm.Controller;

import com.proj.FullForm.Service.FileStorageService;
import org.springframework.core.io.Resource;                 
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.MalformedURLException;

@RestController
@RequestMapping("/api/v1/files")
public class FileController {

    private final FileStorageService storage;

    public FileController(FileStorageService storage) {
        this.storage = storage;
    }

    @GetMapping("/{folder}/{filename:.+}")
    public ResponseEntity<Resource> serve(@PathVariable String folder,
                                          @PathVariable String filename) throws MalformedURLException {
        Resource resource = storage.load(folder + "/" + filename);
        MediaType type = MediaTypeFactory.getMediaType(resource)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);
        return ResponseEntity.ok().contentType(type).body(resource);
    }
}