package com.kremnev.blog.controller;

import com.kremnev.blog.dto.CommentDto;
import com.kremnev.blog.dto.CreateCommentRequest;
import com.kremnev.blog.service.CommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("api/posts/{postId}/comments")
public class CommentsController {
    private final CommentService commentService;

    public CommentsController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("{commentId}")
    public ResponseEntity<CommentDto> getById(@PathVariable long postId, @PathVariable long commentId) {
        var commentOpt = commentService.getById(commentId);
        return ResponseEntity.of(commentOpt.map(CommentDto::from));
    }

    @PostMapping
    public ResponseEntity<CommentDto> addComment(@PathVariable long postId, @RequestBody CreateCommentRequest request) {
        var created = commentService.create(request.postId(), request.text());
        var location = URI.create("/api/posts/" + postId + "/comments/" + created.getId());
        return ResponseEntity
                .created(location)
                .body(CommentDto.from(created));
    }
}
