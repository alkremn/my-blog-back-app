package com.kremnev.blog.dto;

import com.kremnev.blog.model.Comment;

public record CommentDto(
    Long id,
    String text,
    Long postId
) {
    public static CommentDto from(Comment comment) {
        return new CommentDto(
                comment.getId(),
                comment.getText(),
                comment.getPostId()
        );
    }
}
