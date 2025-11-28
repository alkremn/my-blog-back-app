package com.kremnev.blog.dto.Request;

public record CreateCommentRequest(
    Long postId,
    String text
) {}