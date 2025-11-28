package com.kremnev.blog.repository;

import com.kremnev.blog.model.Comment;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class CommentRepositoryImpl implements CommentRepository {

    private final JdbcTemplate jdbc;

    public CommentRepositoryImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static class CommentRowMapper implements RowMapper<Comment> {
        @Override
        public Comment mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Comment(
                    rs.getLong("id"),
                    rs.getLong("post_id"),
                    rs.getString("text"),
                    rs.getObject("created_at", java.time.OffsetDateTime.class),
                    rs.getObject("updated_at", java.time.OffsetDateTime.class)
            );
        }
    }

    @Override
    public List<Comment> findAllByPostId(Long postId) {
        return List.of();
    }

    @Override
    public Optional<Comment> findById(Long commentId) {
        try {
            var comment = jdbc.queryForObject(
                    "SELECT id, post_id, text, created_at, updated_at FROM comments WHERE id = ?",
                    new CommentRowMapper(), commentId);
            if (comment == null)
                return Optional.empty();

            return Optional.of(comment);

        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Comment create(Long postId, String text) {
        Long commentId = jdbc.queryForObject(
                "INSERT INTO comments (post_id, text) " +
                        "VALUES (?, ?) " +
                        "RETURNING id",
                Long.class,
                postId,
                text
        );

        return jdbc.queryForObject(
                "SELECT id, post_id, text, created_at, updated_at " +
                        "FROM comments WHERE id = ?",
                new CommentRowMapper(),
                commentId
        );
    }

    @Override
    public Comment update(Long commentId, Long postId, String text) {
        return null;
    }

    @Override
    public boolean delete(Long commentId) {
        return false;
    }
}