package com.kremnev.blog.dto;

import java.util.List;

public final class PostsResponseDto {
    private List<PostDto> posts;
    private boolean hasPrev;
    private boolean hasNext;
    private int lastPage;

    public PostsResponseDto(List<PostDto> posts, boolean hasPrev, boolean hasNext, int lastPage) {
        this.posts = posts;
        this.hasPrev = hasPrev;
        this.hasNext = hasNext;
        this.lastPage = lastPage;
    }

    public List<PostDto> getPosts() { return posts; }
    public boolean isHasPrev() { return hasPrev; }
    public boolean isHasNext() { return hasNext; }
    public int getLastPage() { return lastPage; }
}
