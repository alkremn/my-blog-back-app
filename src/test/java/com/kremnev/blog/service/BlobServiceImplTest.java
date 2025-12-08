package com.kremnev.blog.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BlobServiceImpl Tests")
class BlobServiceImplTest {

    @TempDir
    Path tempDir;

    private BlobServiceImpl blobService;

    @BeforeEach
    void setUp() {
        blobService = new BlobServiceImpl(tempDir.toString());
    }

    @AfterEach
    void tearDown() throws IOException {
        if (Files.exists(tempDir)) {
            Files.walk(tempDir)
                    .sorted((a, b) -> -a.compareTo(b))
                    .forEach(path -> {
                        try {
                            if (!path.equals(tempDir)) {
                                Files.deleteIfExists(path);
                            }
                        } catch (IOException e) {
                            // Ignore cleanup errors
                        }
                    });
        }
    }

    @Test
    @DisplayName("Should save image file successfully")
    void testUpsertSuccess() throws IOException {
        MultipartFile file = new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        assertDoesNotThrow(() -> blobService.upsert(1L, file));

        Path postDir = tempDir.resolve("1");
        assertTrue(Files.exists(postDir));
        assertTrue(Files.isDirectory(postDir));

        Path imagePath = postDir.resolve("image.jpg");
        assertTrue(Files.exists(imagePath));
        assertTrue(Files.isRegularFile(imagePath));

        String content = Files.readString(imagePath);
        assertEquals("test image content", content);
    }

    @Test
    @DisplayName("Should save image with png extension")
    void testUpsertWithPngExtension() throws IOException {
        MultipartFile file = new MockMultipartFile(
                "image",
                "photo.png",
                "image/png",
                "png content".getBytes()
        );

        blobService.upsert(2L, file);

        Path imagePath = tempDir.resolve("2").resolve("image.png");
        assertTrue(Files.exists(imagePath));
        assertEquals("png content", Files.readString(imagePath));
    }

    @Test
    @DisplayName("Should save image without extension when filename has no extension")
    void testUpsertWithoutExtension() throws IOException {
        MultipartFile file = new MockMultipartFile(
                "image",
                "imagefile",
                "image/jpeg",
                "content".getBytes()
        );

        blobService.upsert(3L, file);

        Path imagePath = tempDir.resolve("3").resolve("image");
        assertTrue(Files.exists(imagePath));
    }

    @Test
    @DisplayName("Should throw IOException when content type is not an image")
    void testUpsertWithInvalidContentType() {
        MultipartFile file = new MockMultipartFile(
                "file",
                "document.pdf",
                "application/pdf",
                "pdf content".getBytes()
        );

        IOException exception = assertThrows(IOException.class, () -> {
            blobService.upsert(1L, file);
        });

        assertEquals("Only image/* allowed", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw IOException when content type is text")
    void testUpsertWithTextContentType() {
        MultipartFile file = new MockMultipartFile(
                "file",
                "text.txt",
                "text/plain",
                "text content".getBytes()
        );

        IOException exception = assertThrows(IOException.class, () -> {
            blobService.upsert(1L, file);
        });

        assertEquals("Only image/* allowed", exception.getMessage());
    }

    @Test
    @DisplayName("Should replace existing image when upserting to same post id")
    void testUpsertReplaceExisting() throws IOException {
        MultipartFile file1 = new MockMultipartFile(
                "image",
                "old.jpg",
                "image/jpeg",
                "old content".getBytes()
        );
        blobService.upsert(1L, file1);

        MultipartFile file2 = new MockMultipartFile(
                "image",
                "new.jpg",
                "image/jpeg",
                "new content".getBytes()
        );
        blobService.upsert(1L, file2);

        Path imagePath = tempDir.resolve("1").resolve("image.jpg");
        assertTrue(Files.exists(imagePath));
        assertEquals("new content", Files.readString(imagePath));
    }

    @Test
    @DisplayName("Should handle uppercase content type")
    void testUpsertWithUppercaseContentType() throws IOException {
        MultipartFile file = new MockMultipartFile(
                "image",
                "test.jpg",
                "IMAGE/JPEG",
                "content".getBytes()
        );

        assertDoesNotThrow(() -> blobService.upsert(1L, file));

        Path imagePath = tempDir.resolve("1").resolve("image.jpg");
        assertTrue(Files.exists(imagePath));
    }

    @Test
    @DisplayName("Should get image resource when image exists")
    void testGetSuccess() throws IOException {
        MultipartFile file = new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                "test content".getBytes()
        );
        blobService.upsert(1L, file);

        Optional<Resource> result = blobService.get(1L);

        assertTrue(result.isPresent());
        Resource resource = result.get();
        assertTrue(resource.exists());
        assertTrue(resource.isFile());
    }

    @Test
    @DisplayName("Should return empty optional when getting image from non-existent post")
    void testGetNonExistentPost() throws IOException {
        Optional<Resource> result = blobService.get(999L);

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Should return empty optional when post directory exists but has no files")
    void testGetEmptyDirectory() throws IOException {
        Path postDir = tempDir.resolve("1");
        Files.createDirectories(postDir);

        Optional<Resource> result = blobService.get(1L);

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Should get first file when multiple files exist in directory")
    void testGetWithMultipleFiles() throws IOException {
        Path postDir = tempDir.resolve("1");
        Files.createDirectories(postDir);
        Files.writeString(postDir.resolve("image1.jpg"), "content1");
        Files.writeString(postDir.resolve("image2.jpg"), "content2");

        Optional<Resource> result = blobService.get(1L);

        assertTrue(result.isPresent());
        assertTrue(result.get().exists());
    }

    @Test
    @DisplayName("Should create nested directory structure when upserting")
    void testUpsertCreatesDirectories() throws IOException {
        assertFalse(Files.exists(tempDir.resolve("100")));

        MultipartFile file = new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                "content".getBytes()
        );
        blobService.upsert(100L, file);

        assertTrue(Files.exists(tempDir.resolve("100")));
        assertTrue(Files.exists(tempDir.resolve("100").resolve("image.jpg")));
    }

    @Test
    @DisplayName("Should handle file with multiple dots in name")
    void testUpsertWithMultipleDotsInFilename() throws IOException {
        MultipartFile file = new MockMultipartFile(
                "image",
                "my.image.file.jpg",
                "image/jpeg",
                "content".getBytes()
        );

        blobService.upsert(1L, file);

        Path imagePath = tempDir.resolve("1").resolve("image.jpg");
        assertTrue(Files.exists(imagePath));
    }

    @Test
    @DisplayName("Should handle null original filename gracefully")
    void testUpsertWithNullFilename() throws IOException {
        MultipartFile file = new MockMultipartFile(
                "image",
                null,
                "image/jpeg",
                "content".getBytes()
        );

        blobService.upsert(1L, file);

        Path postDir = tempDir.resolve("1");
        assertTrue(Files.exists(postDir));
        Path imagePath = postDir.resolve("image");
        assertTrue(Files.exists(imagePath));
    }

    @Test
    @DisplayName("Should preserve image content integrity")
    void testImageContentIntegrity() throws IOException {
        byte[] originalContent = "binary image data \0\1\2\3".getBytes();
        MultipartFile file = new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                originalContent
        );

        blobService.upsert(1L, file);

        Path imagePath = tempDir.resolve("1").resolve("image.jpg");
        byte[] savedContent = Files.readAllBytes(imagePath);
        assertArrayEquals(originalContent, savedContent);
    }

    @Test
    @DisplayName("Should handle different image formats")
    void testDifferentImageFormats() throws IOException {
        String[] formats = {"image/jpeg", "image/png", "image/gif", "image/webp", "image/svg+xml"};
        String[] extensions = {".jpg", ".png", ".gif", ".webp", ".svg"};

        for (int i = 0; i < formats.length; i++) {
            MultipartFile file = new MockMultipartFile(
                    "image",
                    "test" + extensions[i],
                    formats[i],
                    "content".getBytes()
            );

            long postId = i + 1;
            assertDoesNotThrow(() -> blobService.upsert(postId, file));

            Path imagePath = tempDir.resolve(String.valueOf(postId)).resolve("image" + extensions[i]);
            assertTrue(Files.exists(imagePath), "Image with format " + formats[i] + " should exist");
        }
    }
}
