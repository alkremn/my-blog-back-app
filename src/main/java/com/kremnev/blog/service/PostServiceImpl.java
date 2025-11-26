package com.kremnev.blog.service;

import com.kremnev.blog.model.Post;
import com.kremnev.blog.model.PostsResponse;
import com.kremnev.blog.repository.PostRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PostServiceImpl implements PostService {
    private final PostRepository postRepository;

    public PostServiceImpl(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Override
    public PostsResponse getAll(String search, int pageNumber, int pageSize) {
        var result = postRepository.findAll(search, pageNumber, pageSize);
        var posts = result.getFirst();
        var totalCount = result.getSecond();
        return new PostsResponse(posts, pageNumber, pageSize, totalCount);
    }

    @Override
    public Optional<Post> getById(long id) {
        return postRepository.findById(id);
    }

    @Override
    public Post create(String title, String text, List<String> tags) {
        return null;
    }

    @Override
    public Post update(Long id, String title, String text, List<String> tags) {
        return null;
    }
}
