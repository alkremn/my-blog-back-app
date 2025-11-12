package com.kremnev.blog.repository;

import com.kremnev.blog.model.Post;

import java.util.List;
import java.util.Optional;

public interface PostRepository {
    List<Post> findAll();
    Optional<Post> findById(long id);
}
