package com.kremnev.blog.dto.Request;

public record UpdateCommentRequest(
    Long commentId,
    String text,
    Long postId
) {}
