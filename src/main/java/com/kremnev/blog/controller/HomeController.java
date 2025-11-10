package com.kremnev.blog.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/home")
    @ResponseBody
    public String homePage() {
        return "<h1>Hello, world!</h1>";
    }
}
