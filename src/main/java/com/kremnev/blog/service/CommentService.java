package com.kremnev.blog.service;

import com.kremnev.blog.model.Comment;

import java.util.List;
import java.util.Optional;

public interface CommentService {
    List<Comment> getAllByPostId(Long postId);
    Optional<Comment> getById(Long commentId);
    Comment create(Long postId, String text);
    Optional<Comment> update(Long commentId, Long postId, String text);
    boolean delete(Long commentId, Long postId);
}