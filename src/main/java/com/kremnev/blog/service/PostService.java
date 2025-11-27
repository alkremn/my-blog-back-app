package com.kremnev.blog.service;

import com.kremnev.blog.model.Post;
import com.kremnev.blog.model.PostsResponse;

import java.util.List;
import java.util.Optional;

public interface PostService {
    PostsResponse getAll(String search, int pageNumber, int pageSize);
    Optional<Post> getById(long postId);
    Post create(String title, String text, List<String> tags);
    Optional<Post> update(Long postId, String title, String text, List<String> tags);
    Optional<Post> addLike(Long postId);
}
