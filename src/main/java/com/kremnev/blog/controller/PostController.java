package com.kremnev.blog.controller;

import com.kremnev.blog.dto.PostDto;
import com.kremnev.blog.service.PostService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    @ResponseBody
    public List<PostDto> getPosts() {
        return postService.findAll();
    }

    @GetMapping(value = "/{id}")
    public PostDto getPost(long id) {
        return postService.findById(id);
    }
}
