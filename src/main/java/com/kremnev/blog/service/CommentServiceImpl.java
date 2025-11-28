package com.kremnev.blog.service;

import com.kremnev.blog.model.Comment;
import com.kremnev.blog.repository.CommentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;

    public CommentServiceImpl(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    @Override
    public List<Comment> getAllByPostId(Long postId) {
        return List.of();
    }

    @Override
    public Optional<Comment> getById(Long commentId) {
        return commentRepository.findById(commentId);
    }

    @Override
    public Comment create(Long postId, String text) {
        return commentRepository.create(postId, text);
    }

    @Override
    public Comment update(Long commentId, Long postId, String text) {
        return null;
    }

    @Override
    public boolean delete(Long commentId) {
        return false;
    }
}