package com.kremnev.blog.dto;

import com.kremnev.blog.model.Comment;

public record CommentDto(
    Long id,
    Long postId,
    String text
) {
    public static CommentDto from(Comment comment) {
        return new CommentDto(
                comment.getId(),
                comment.getPostId(),
                comment.getText()
        );
    }
}
