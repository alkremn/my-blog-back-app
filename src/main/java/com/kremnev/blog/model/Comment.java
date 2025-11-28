package com.kremnev.blog.model;

import java.time.OffsetDateTime;

public final class Comment {
    private final Long id;
    private final Long postId;
    private final String text;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime updatedAt;

    public Comment(Long id, Long postId, String text, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.postId = postId;
        this.text = text;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getPostId() {
        return postId;
    }

    public String getText() {
        return text;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}