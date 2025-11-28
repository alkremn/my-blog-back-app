package com.kremnev.blog.controller;

import com.kremnev.blog.dto.CommentDto;
import com.kremnev.blog.dto.Request.CreateCommentRequest;
import com.kremnev.blog.dto.Request.UpdateCommentRequest;
import com.kremnev.blog.service.CommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("api/posts/{postId}/comments")
public class CommentsController {
    private final CommentService commentService;

    public CommentsController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    public ResponseEntity<List<CommentDto>> getAllByPostId(@PathVariable long postId) {
        var comments = commentService.getAllByPostId(postId);
        return ResponseEntity.ok(comments.stream().map(CommentDto::from).toList());
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

    @PutMapping("{commentId}")
    public ResponseEntity<CommentDto> updateComment(@PathVariable long postId, @PathVariable long commentId, @RequestBody UpdateCommentRequest request) {
        var updatedOpt = commentService.update(commentId, postId, request.text());
        return updatedOpt.map(comment -> {
            var location = URI.create("/api/posts/" + postId + "/comments/" + comment.getId());
            return ResponseEntity
                    .created(location)
                    .body(CommentDto.from(comment));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable long postId, @PathVariable long commentId) {
        var isDeleted = commentService.delete(commentId, postId);
        return isDeleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
