package com.kremnev.blog.service;

import com.kremnev.blog.model.Post;

import java.util.List;

public interface PostService {
    List<Post> findAll();
}
