package com.kremnev.blog.service;

import com.kremnev.blog.model.Post;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface PostService {
    List<Post> getAll(String search, int pageNumber, int pageSize);
    Optional<Post> getById(long id);
    Post create(String title, String text, List<String> tags);
    Post update(Long id, String title, String text, List<String> tags);
}
