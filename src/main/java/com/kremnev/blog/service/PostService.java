package com.kremnev.blog.service;

import com.kremnev.blog.dto.PostDto;
import com.kremnev.blog.dto.PostsResponseDto;

import java.util.Optional;

public interface PostService {
    PostsResponseDto findAll(String search, int pageNumber, int pageSize);
    Optional<PostDto> findById(long id);
}
