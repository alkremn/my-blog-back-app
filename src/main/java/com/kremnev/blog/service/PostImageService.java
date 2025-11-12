package com.kremnev.blog.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

public interface PostImageService {
    void upsert(long postId, MultipartFile file) throws IOException;
    Optional<Resource> get(long postId) throws IOException;
}
