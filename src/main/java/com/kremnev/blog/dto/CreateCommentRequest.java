package com.kremnev.blog.dto;

public record CreateCommentRequest(
    Long postId,
    String text
) {}