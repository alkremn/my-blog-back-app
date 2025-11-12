package com.kremnev.blog.service;

import com.kremnev.blog.dto.PostDto;
import com.kremnev.blog.dto.PostsResponseDto;
import com.kremnev.blog.repository.PostRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PostServiceImpl implements PostService {

    public static final String UPLOAD_DIR = "uploads/";
    private final PostRepository postRepository;

    public PostServiceImpl(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Override
    public PostsResponseDto findAll(String search, int pageNumber, int pageSize) {
        var posts = postRepository.findAll().stream().map(post -> new PostDto(
                post.getId(),
                post.getTitle(),
                post.getText(),
                post.getTags(),
                post.getLikesCount(),
                post.getCommentsCount()))
                .toList();

        return new PostsResponseDto(posts, false, false, 1);
    }

    @Override
    public Optional<PostDto> findById(long id) {
        var optionalPost = postRepository.findById(id);
        if (optionalPost.isEmpty())
            return Optional.empty();

        var post = optionalPost.get();
        return Optional.of(new PostDto(
                post.getId(),
                post.getTitle(),
                post.getText(),
                post.getTags(),
                post.getLikesCount(),
                post.getCommentsCount()));
    }
}
