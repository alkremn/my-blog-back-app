package com.kremnev.blog.dto;

import java.util.List;

public final class PostDto {
    private final Long id;
    private final String title;
    private final String text;
    private final List<String> tags;
    private final int likesCount;
    private final int commentsCount;

    public PostDto(Long id, String title, String text, List<String> tags, int likesCount, int commentsCount) {
        this.id = id;
        this.title = title;
        this.text = text;
        this.tags = tags;
        this.likesCount = likesCount;
        this.commentsCount = commentsCount;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public List<String> getTags() {
        return tags;
    }

    public int getLikesCount() {
        return likesCount;
    }

    public int getCommentsCount() {
        return commentsCount;
    }
}
