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
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        String contentType = file.getContentType();
        if (allowedTypes != null && !allowedTypes.isEmpty() && contentType != null) {
            String lowerType = contentType.toLowerCase();
            boolean matched = allowedTypes.contains(lowerType) ||
                    allowedTypes.stream().anyMatch(t -> {
                        if (t.endsWith("/*")) {
                            return lowerType.startsWith(t.substring(0, t.length() - 1));
                        }
                        return false;
                    });
            if (!matched) {
                throw new IllegalArgumentException("Unsupported file type: " + contentType);
            }
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

    public String storeText(String content, String folder, String filename) throws IOException {
        Path dir = root.resolve(folder);
        Files.createDirectories(dir);
        Path target = dir.resolve(filename);
        Files.writeString(target, content, java.nio.charset.StandardCharsets.UTF_8);
        return folder + "/" + filename;
    }

    public String readText(String relativePath) throws IOException {
        if (relativePath == null || relativePath.isBlank()) return null;
        String cleanPath = cleanRelativePath(relativePath);
        Path file = root.resolve(cleanPath).normalize();
        if (!file.startsWith(root) || !Files.exists(file)) {
            return null;
        }
        return Files.readString(file, java.nio.charset.StandardCharsets.UTF_8);
    }

    public Resource load(String relativePath) throws MalformedURLException {
        String cleanPath = cleanRelativePath(relativePath);
        Path file = root.resolve(cleanPath).normalize();
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
        if (relativePath == null || relativePath.isBlank()) return;
        String cleanPath = cleanRelativePath(relativePath);
        Path file = root.resolve(cleanPath).normalize();
        if (file.startsWith(root)) {
            Files.deleteIfExists(file);
        }
    }

    public Path getRoot() {
        return root;
    }

    private String cleanRelativePath(String path) {
        if (path.startsWith("/uploads/")) {
            return path.substring("/uploads/".length());
        }
        if (path.startsWith("uploads/")) {
            return path.substring("uploads/".length());
        }
        if (path.startsWith("/api/v1/files/")) {
            return path.substring("/api/v1/files/".length());
        }
        return path;
    }
}
