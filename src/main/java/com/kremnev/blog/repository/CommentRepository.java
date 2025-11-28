package com.kremnev.blog.repository;

import com.kremnev.blog.model.Comment;

import java.util.List;
import java.util.Optional;

public interface CommentRepository {
    List<Comment> findAllByPostId(Long postId);
    Optional<Comment> findById(Long commentId);
    Comment create(Long postId, String text);
    Comment update(Long commentId, Long postId, String text);
    boolean delete(Long commentId);
}
