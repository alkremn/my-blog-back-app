package com.kremnev.blog.dto;

import java.util.List;

public record CreatePostRequest(
    String title,
    String text,
    List<String> tags
) {}

