package com.kremnev.blog.repository;

import com.kremnev.blog.model.Post;

import java.util.List;

public interface PostRepository {
    List<Post> findAll();
}
