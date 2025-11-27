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
    public Optional<Post> getById(long postId) {
        return postRepository.findById(postId);
    }

    @Override
    public Post create(String title, String text, List<String> tags) {
        return postRepository.create(title, text, tags);
    }

    @Override
    public Optional<Post> update(Long postId, String title, String text, List<String> tags) {
        return postRepository.update(postId, title, text, tags);
    }

    @Override
    public boolean delete(Long postId) {
        return postRepository.delete(postId);
    }

    @Override
    public Optional<Post> addLike(Long postId) {
        return postRepository.addLike(postId);
    }
}
