package com.kremnev.blog.controller;

import com.kremnev.blog.service.BlobService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("api/posts/{postId}/image")
public class ImageController {
    private final BlobService blobService;

    public ImageController(BlobService blobService) {
        this.blobService = blobService;
    }

    @PutMapping
    public ResponseEntity<?> updatePostImage(@PathVariable long postId, @RequestParam("image") MultipartFile image) {
        if (image == null)
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "File is empty"));

        try {
            blobService.upsert(postId, image);
            return ResponseEntity.noContent().build();
        } catch (IOException ex) {
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                    .body(Map.of("message", ex.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<Resource> downloadPostImage(@PathVariable long postId) {
        try {
            Optional<Resource> file = blobService.get(postId);
            return file.map(resource -> ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/octet-stream"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource)).orElseGet(() -> ResponseEntity.notFound().build());

        } catch (IOException ex) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
