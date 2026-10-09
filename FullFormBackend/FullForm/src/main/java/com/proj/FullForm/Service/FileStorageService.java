package com.proj.FullForm.Service;

import org.springframework.beans.factory.annotation.Value;   
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path root;

    public FileStorageService(@Value("${app.upload-dir}") String dir) throws IOException {
        this.root = Paths.get(dir).toAbsolutePath().normalize();
        Files.createDirectories(root);
    }

    public String store(MultipartFile file, String folder, Set<String> allowedTypes) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        if (file.getContentType() == null || !allowedTypes.contains(file.getContentType())) {
            throw new IllegalArgumentException("Unsupported file type: " + file.getContentType());
        }

        String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID() + (ext != null ? "." + ext.toLowerCase() : "");

        Path dir = root.resolve(folder);
        Files.createDirectories(dir);
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, dir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
        }
        return folder + "/" + filename;
    }

    public Resource load(String relativePath) throws MalformedURLException {
        Path file = root.resolve(relativePath).normalize();
        if (!file.startsWith(root)) {                       // blocks ../../ tricks
            throw new IllegalArgumentException("Invalid path");
        }
        Resource resource = new UrlResource(file.toUri());
        if (!resource.exists()) {
            throw new RuntimeException("File not found");
        } else {
            return resource;
        }
    }

    public void delete(String relativePath) throws IOException {
        if (relativePath == null) return;
        Path file = root.resolve(relativePath).normalize();
        if (file.startsWith(root)) {
            Files.deleteIfExists(file);
        }
    }
}
