package com.kremnev.blog.service;

import com.kremnev.blog.dto.PostDto;
import com.kremnev.blog.repository.PostRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;

    public PostServiceImpl(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Override
    public List<PostDto> findAll() {
        return postRepository.findAll().stream().map(post -> new PostDto(
                post.getId(),
                post.getTitle(),
                post.getText(),
                post.getTags(),
                post.getLikesCount(),
                post.getCommentsCount()))
                .toList();
    }

    @Override
    public PostDto findById(long id) {
        var post = postRepository.findById(id);
        return new PostDto(
                post.getId(),
                post.getTitle(),
                post.getText(),
                post.getTags(),
                post.getLikesCount(),
                post.getCommentsCount());
    }
}
