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
        return jdbc.query("SELECT * FROM comments WHERE post_id = ?", new CommentRowMapper(), postId);
    }

    @Override
    public Optional<Comment> findById(Long commentId) {
        try {
            var comment = jdbc.queryForObject(
                    "SELECT * FROM comments WHERE id = ?",
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
                "SELECT * FROM comments WHERE id = ?",
                new CommentRowMapper(),
                commentId
        );
    }

    @Override
    public Optional<Comment> update(Long commentId, Long postId, String text) {
        int rows = jdbc.update(
            "UPDATE comments SET text = ?, updated_at = NOW() WHERE id = ? AND post_id = ?",
            text,
            commentId,
            postId
        );

        if (rows == 0) return Optional.empty();

        return findById(commentId);
    }

    @Override
    public boolean delete(Long commentId, Long postId) {
        int rows = jdbc.update("DELETE FROM comments WHERE id = ? AND post_id = ?", commentId, postId);
        return rows > 0;
    }
}