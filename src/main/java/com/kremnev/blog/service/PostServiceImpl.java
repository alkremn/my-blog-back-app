package com.kremnev.blog.service;

import com.kremnev.blog.model.Post;
import com.kremnev.blog.repository.PostRepository;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PostServiceImpl implements PostService {

    public static final String UPLOAD_DIR = "uploads/";
    private final PostRepository postRepository;

    public PostServiceImpl(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Override
    public List<Post> getAll(String search, int pageNumber, int pageSize) {
        return postRepository.findAll(search, pageNumber, pageSize);
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
