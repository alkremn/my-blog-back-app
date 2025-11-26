package com.kremnev.blog.repository;

import com.kremnev.blog.model.Post;
import org.springframework.data.util.Pair;

import java.util.List;
import java.util.Optional;

public interface PostRepository {
    Pair<List<Post>, Integer> findAll(String search, int pageNumber, int pageSize);
    Optional<Post> findById(long id);
    Post create(String title, String text, List<String> tags);
}
