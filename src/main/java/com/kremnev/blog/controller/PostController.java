package com.kremnev.blog.controller;

import com.kremnev.blog.dto.*;
import com.kremnev.blog.dto.Request.CreatePostRequest;
import com.kremnev.blog.dto.Request.UpdatePostRequest;
import com.kremnev.blog.model.PostsResponse;
import com.kremnev.blog.service.PostImageService;
import com.kremnev.blog.service.PostService;
import org.springframework.core.io.Resource;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.util.List;
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
    public PostsResponseDto getAllPosts(@RequestParam(required = false) String search,
                                     @RequestParam(defaultValue = "1") int pageNumber,
                                     @RequestParam(defaultValue = "25") int pageSize)
    {
        PostsResponse result = postService.getAll(search, pageNumber, pageSize);
        List<PostDto> dtoList = result.getPosts().stream().map(PostDto::from).toList();
        return new PostsResponseDto(dtoList, result.getHasPrev(), result.getHasNext(), result.getLastPage());
    }

    @GetMapping("{postId}")
    public ResponseEntity<PostDto> getPostById(@PathVariable long postId) {
        var postOpt = postService.getById(postId);
        return ResponseEntity.of(postOpt.map(PostDto::from));
    }

    @PostMapping
    public ResponseEntity<PostDto> createPost(@RequestBody CreatePostRequest request) {
        var created = postService.create(request.title(), request.text(), request.tags());
        var location = URI.create("/api/posts/" + created.getId());
        return ResponseEntity
                .created(location)
                .body(PostDto.from(created));
    }

    @PutMapping("{postId}")
    public ResponseEntity<PostDto> updatePost(@PathVariable long postId, @RequestBody UpdatePostRequest request) {
        var updated = postService.update(postId, request.title(), request.text(), request.tags());
        return updated.map(post -> {
            var location = URI.create("/api/posts/" + post.getId());
            return ResponseEntity.created(location).body(PostDto.from(post));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable long postId) {
        var isDeleted = postService.delete(postId);
        if (!isDeleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping("{postId}/likes")
    public ResponseEntity<PostDto> addLike(@PathVariable long postId) {
        var updated = postService.addLike(postId);
        return updated.map(post -> {
            var location = URI.create("/api/posts/" + post.getId());
            return ResponseEntity.created(location).body(PostDto.from(post));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("{postId}/image")
    public ResponseEntity<?> updatePostImage(@PathVariable long postId, @RequestParam("image") MultipartFile image) {
        if (image == null)
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "File is empty"));

        try {
            postImageService.upsert(postId, image);
            return ResponseEntity.noContent().build();
        } catch (IOException ex) {
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                    .body(Map.of("message", ex.getMessage()));
        }
    }

    @GetMapping("{postId}/image")
    public ResponseEntity<Resource> downloadPostImage(@PathVariable long postId) {
        try {
            Optional<Resource> file = postImageService.get(postId);
            return file.map(resource -> ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/octet-stream"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource)).orElseGet(() -> ResponseEntity.notFound().build());

        } catch (IOException ex) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
