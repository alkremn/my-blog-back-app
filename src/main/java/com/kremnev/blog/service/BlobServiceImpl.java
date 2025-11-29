package com.kremnev.blog.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Service
public class BlobServiceImpl implements BlobService {

    private final Path root;

    public BlobServiceImpl(@Value("${uploads.root}") String rootDir) {
        this.root = Path.of(rootDir).toAbsolutePath().normalize();
    }

    @Override
    public void upsert(long postId, MultipartFile file) throws IOException {
        Path dir = root.resolve(Long.toString(postId));
        Files.createDirectories(dir);

        String ct = String.valueOf(file.getContentType()).toLowerCase();
        if (!ct.startsWith("image/")) throw new IOException("Only image/* allowed");

        String ext = Optional.ofNullable(file.getOriginalFilename())
                .map(p -> Path.of(p).getFileName().toString())
                .filter(n -> n.contains("."))
                .map(n -> n.substring(n.lastIndexOf('.')))
                .orElse("");

        Path dest = dir.resolve("image" + ext);
        file.transferTo(dest);
    }

    @Override
    public Optional<Resource> get(long postId) throws IOException {
        Path dir = root.resolve(Long.toString(postId));
        if (!Files.isDirectory(dir)) return Optional.empty();
        try (var s = Files.list(dir)) {
            var pathOpt =  s.filter(Files::isRegularFile).findFirst();
            if (pathOpt.isEmpty()) return Optional.empty();

            Path path = pathOpt.get();
            Resource file = new FileSystemResource(path);
            return Optional.of(file);
        }
    }
}
