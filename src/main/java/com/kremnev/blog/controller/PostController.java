package com.kremnev.blog.controller;

import com.kremnev.blog.dto.*;
import com.kremnev.blog.dto.Request.CreatePostRequest;
import com.kremnev.blog.dto.Request.UpdatePostRequest;
import com.kremnev.blog.model.PostsResponse;
import com.kremnev.blog.service.PostService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public PostsResponseDto getAllPosts(@RequestParam(required = false) String search,
                                     @RequestParam(defaultValue = "1") int pageNumber,
                                     @RequestParam(defaultValue = "5") int pageSize)
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
        return updated.map(post -> ResponseEntity.ok().body(PostDto.from(post)))
                .orElseGet(() -> ResponseEntity.notFound().build());
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
            return ResponseEntity.ok().body(PostDto.from(post));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
