package com.kremnev.blog.controller;

import com.kremnev.blog.dto.PostDto;
import com.kremnev.blog.dto.PostsResponseDto;
import com.kremnev.blog.service.PostImageService;
import com.kremnev.blog.service.PostService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("api/posts")
public class PostController {

    private final PostService postService;
    private final PostImageService postImageService;

    public PostController(PostService postService, PostImageService postImageService) {
        this.postService = postService;
        this.postImageService = postImageService;
    }

    @GetMapping
    @ResponseBody
    public PostsResponseDto getPosts(@RequestParam(required = false) String search,
                                     @RequestParam(defaultValue = "1") int pageNumber,
                                     @RequestParam(defaultValue = "25") int pageSize)
    {
       return postService.findAll(search, pageNumber, pageSize);
    }

    @GetMapping(value = "{id}")
    public ResponseEntity<PostDto> getPost(@PathVariable(name = "id") long id) {
        var post = postService.findById(id);
        return ResponseEntity.of(post);
    }

    @PutMapping(value = "{postId}/image")
    public ResponseEntity<?> updatePostImage(@PathVariable(name = "postId") long postId, @RequestParam("file") MultipartFile file) {
        try {
            postImageService.upsert(postId, file);
            return ResponseEntity.ok().build();
        } catch (IOException ex) {
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                    .body(Map.of("message", ex.getMessage()));
        }
    }

    @GetMapping(value = "{postId}/image")
    public ResponseEntity<Resource> downloadPostImage(@PathVariable(name = "postId") long postId) {
        try {
            Optional<Resource> file = postImageService.get(postId);
            return file.map(resource -> ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/octet-stream"))
                    .body(resource)).orElseGet(() -> ResponseEntity.notFound().build());

        } catch (IOException ex) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
