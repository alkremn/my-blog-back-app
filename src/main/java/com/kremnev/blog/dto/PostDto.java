package com.kremnev.blog.dto;

import com.kremnev.blog.model.Post;

import java.util.List;

public record PostDto(
    Long id,
    String title,
    String text,
    List<String> tags,
    int likesCount,
    int commentsCount
) {
    public static PostDto from(Post post) {
        return new PostDto(
                post.getId(),
                post.getTitle(),
                post.getText(),
                post.getTags(),
                post.getLikesCount(),
                post.getCommentsCount()
        );
    }
}
