package com.ecom.proj.service;

import com.ecom.proj.exception.ApiException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {

    private static final Set<String> ALLOWED_MIME = Set.of("image/jpeg","image/png","image/webp","image/gif");
    private static final Set<String> ALLOWED_EXT  = Set.of("jpg","jpeg","png","webp","gif");
    private static final long MAX_BYTES = 5L * 1024 * 1024;
    public static final int MIN_IMAGES = 3;
    public static final int MAX_IMAGES = 5;

    @Value("${app.upload.dir}") private String uploadDir;
    @Value("${app.upload.base-url}") private String baseUrl;
    private Path root;

    @PostConstruct
    void init() throws IOException {
        root = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(root);
        log.info("Upload root: {}", root);
    }

    public void validateBatch(List<MultipartFile> images) {
        if (images == null || images.size() < MIN_IMAGES)
            throw ApiException.badRequest("At least " + MIN_IMAGES + " images are required");
        if (images.size() > MAX_IMAGES)
            throw ApiException.badRequest("Maximum " + MAX_IMAGES + " images allowed");
        images.forEach(this::validateSingle);
    }

    private void validateSingle(MultipartFile f) {
        if (f == null || f.isEmpty()) throw ApiException.badRequest("One of the images is empty");
        if (f.getSize() > MAX_BYTES) throw ApiException.badRequest("Each image must be at most 5MB");
        String mime = f.getContentType() == null ? "" : f.getContentType().toLowerCase(Locale.ROOT);
        if (!ALLOWED_MIME.contains(mime)) throw ApiException.badRequest("Unsupported image type: " + mime);
        if (!ALLOWED_EXT.contains(extension(f.getOriginalFilename())))
            throw ApiException.badRequest("Unsupported file extension");
    }

    public String storeVehicleImage(Long vehicleId, MultipartFile file) {
        try {
            Path dir = root.resolve("vehicles").resolve(String.valueOf(vehicleId));
            Files.createDirectories(dir);
            String filename = UUID.randomUUID().toString().replace("-", "") + "." + extension(file.getOriginalFilename());
            Path target = dir.resolve(filename);
            try (var in = file.getInputStream()) { Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING); }
            return baseUrl + "/vehicles/" + vehicleId + "/" + filename;
        } catch (IOException e) {
            log.error("Failed to store image", e);
            throw new ApiException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store image");
        }
    }

    public void deleteVehicleFolder(Long vehicleId) {
        try {
            Path dir = root.resolve("vehicles").resolve(String.valueOf(vehicleId));
            if (!Files.exists(dir)) return;
            try (var walk = Files.walk(dir)) {
                walk.sorted((a, b) -> b.compareTo(a)).forEach(p -> { try { Files.deleteIfExists(p); } catch (IOException ignored) {} });
            }
        } catch (IOException e) { log.warn("Could not delete upload folder for vehicle {}", vehicleId, e); }
    }

    private static String extension(String name) {
        if (name == null) return "";
        int i = name.lastIndexOf('.');
        return i < 0 ? "" : name.substring(i + 1).toLowerCase(Locale.ROOT);
    }
}
