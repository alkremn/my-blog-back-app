package com.kremnev.blog.service;

import com.kremnev.blog.dto.PostDto;

import java.util.List;

public interface PostService {
    List<PostDto> findAll();
    PostDto findById(long id);
}
