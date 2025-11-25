package com.kremnev.blog.model;

import java.util.List;

public final class Post {
    private final Long id;
    private final String title;
    private final String text;
    private List<String> tags;
    private final int likesCount;

    public Post(Long id, String title, String text, List<String> tags, int likesCount, int commentsCount) {
        this.id = id;
        this.title = title;
        this.text = text;
        this.tags = tags;
        this.likesCount = likesCount;
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

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public int getLikesCount() {
        return likesCount;
    }
}



