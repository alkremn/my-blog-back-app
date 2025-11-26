package com.kremnev.blog.model;

import java.util.List;

public final class PostsResponse {
private final List<Post> posts;
    private final boolean hasPrev;
    private final boolean hasNext;
    private final int lastPage;

    public PostsResponse(List<Post> posts, int pageNumber, int pageSize, long totalCount) {
        this.posts = posts;
        this.lastPage = (int) Math.max(1, Math.ceil(totalCount / (double) pageSize));
        this.hasPrev = pageNumber > 1;
        this.hasNext = pageNumber < this.lastPage;
    }

    public List<Post> getPosts() {
        return posts;
    }

    public boolean getHasPrev() {
        return hasPrev;
    }

    public boolean getHasNext() {
        return hasNext;
    }

    public int getLastPage() {
        return lastPage;
    }
}
