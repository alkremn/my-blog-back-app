package com.kremnev.blog.repository;

import com.kremnev.blog.model.Post;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class PostRepositoryImpl implements PostRepository {

    @Override
    public List<Post> findAll() {
        var users = new ArrayList<Post>();
        for (long i = 1; i <= 10; i++) {
            var post = new Post(i, "Post Title #" + i, "This is a post", new ArrayList<String>(), 0, 0);
            users.add(post);
        }

        return users;
    }
}
