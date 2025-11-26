package com.kremnev.blog.dto;

import java.util.List;

public record PostsResponseDto(
    List<PostDto> posts,
    boolean hasPrev,
    boolean hasNext,
    int lastPage
) {}


