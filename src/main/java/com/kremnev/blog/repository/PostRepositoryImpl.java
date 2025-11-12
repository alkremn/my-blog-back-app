package com.kremnev.blog.repository;

import com.kremnev.blog.model.Post;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class PostRepositoryImpl implements PostRepository {

    private final JdbcTemplate jdbcTemplate;

    public PostRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Post> findAll() {
        return jdbcTemplate.query(
                "SELECT id, title, text, likesCount, commentsCount FROM posts",
                (rs, rowNum) -> new Post(
                        rs.getLong("id"),
                        rs.getString("title"),
                        rs.getString("text"),
                        new ArrayList<String>(),
                        rs.getInt("likesCount"),
                        rs.getInt("commentsCount")
                ));
    }

    @Override
    public Optional<Post> findById(long id) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT id, title, text, likesCount, commentsCount FROM posts WHERE id = ?",
                    (rs, rowNum) -> Optional.of(new Post(
                            rs.getLong("id"),
                            rs.getString("title"),
                            rs.getString("text"),
                            new ArrayList<String>(),
                            rs.getInt("likesCount"),
                            rs.getInt("commentsCount"))
                    ), id);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
