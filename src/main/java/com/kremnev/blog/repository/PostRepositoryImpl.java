package com.kremnev.blog.repository;

import com.kremnev.blog.model.Post;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.util.Pair;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class PostRepositoryImpl implements PostRepository {

    private final JdbcTemplate jdbc;

    public PostRepositoryImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static class SearchCriteria {
        final String titleQuery;
        final List<String> tags;

        SearchCriteria(String titleQuery, List<String> tags) {
            this.titleQuery = titleQuery;
            this.tags = tags;
        }
    }

    private static class PostRowMapper implements RowMapper<Post> {
        @Override
        public Post mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Post(
                    rs.getLong("id"),
                    rs.getString("title"),
                    rs.getString("text"),
                    new ArrayList<String>(),
                    rs.getInt("likes_count"),
                    rs.getInt("comments_count")
            );
        }
    }

    @Override
    public Pair<List<Post>, Integer> findAll(String search, int pageNumber, int pageSize) {
        SearchCriteria sc = parseSearch(search);

        boolean filterByTags = !sc.tags.isEmpty();
        boolean filterByTitle = sc.titleQuery != null && !sc.titleQuery.isBlank();

        StringBuilder sql = new StringBuilder(
            "SELECT p.id, p.title, p.text, p.likes_count, COUNT(c.id) as comments_count," +
                    "p.created_at, p.updated_at, count(*) over() as total_count FROM posts p " +
                    "LEFT JOIN comments c ON c.post_id = p.id "
        );

        List<Object> args = new ArrayList<>();

        if (filterByTags) {
            sql.append("JOIN posts_tags pt ON pt.post_id = p.id ")
                    .append("JOIN tags t ON t.id = pt.tag_id ");
        }

        List<String> whereParts = new ArrayList<>();

        if (filterByTitle) {
            whereParts.add("p.title ILIKE ?");
            args.add("%" + sc.titleQuery + "%");
        }

        if (filterByTags) {
            String placeholders = String.join(", ", Collections.nCopies(sc.tags.size(), "?"));
            whereParts.add("LOWER(t.name) IN (" + placeholders + ") ");
            args.addAll(sc.tags);
        }

        if (!whereParts.isEmpty()) {
            sql.append("WHERE ")
                    .append(String.join(" AND ", whereParts))
                    .append(" ");
        }

        sql.append("GROUP BY p.id ");

        if (filterByTags) {
            sql.append("HAVING COUNT(DISTINCT LOWER(t.name)) = ? ");
            args.add(sc.tags.size());
        }

        sql.append("ORDER BY p.created_at DESC ");

        int totalCount = getTotalCount(whereParts, args);
        int offset = Math.max(pageNumber -  1, 0) * pageSize;
        sql.append("LIMIT ? OFFSET ?");
        args.add(pageSize);
        args.add(offset);

        List<Post> posts = jdbc.query(sql.toString(), args.toArray(), new PostRowMapper());
        attachTags(posts);

        return Pair.of(posts, totalCount);
    }

    @Override
    public Optional<Post> findById(long id) {
        try {
            var post = jdbc.queryForObject(
                    "SELECT p.id, p.title, p.text, p.likes_count, COUNT(c.id) as comments_count, p.created_at, p.updated_at " +
                            "FROM posts p " +
                            "LEFT JOIN comments c ON c.post_id = p.id " +
                            "WHERE p.id = ? " +
                            "GROUP BY p.id ",
                    new PostRowMapper(), id);
            if (post == null)
                return Optional.empty();

            attachTags(post);
            return Optional.of(post);

        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Post create(String title, String text, List<String> tags) {
        return null;
    }

    private void attachTags(Post... posts) {
        if (posts == null || posts.length == 0) return;

        List<Post> list = Arrays.asList(posts);
        attachTags(list);
    }

    private void attachTags(List<Post> posts) {
        if (posts.isEmpty())
            return;

        Map<Long, Post> postIdToPost = posts.stream().collect(Collectors.toMap(Post::getId, p -> p));
        List<Long> postIds = new ArrayList<>(postIdToPost.keySet());

        String placeholders = postIds.stream()
                .map(id -> "?")
                .collect(Collectors.joining(", "));

        String sql = """
            SELECT pt.post_id, t.name
            FROM post_tags pt
            JOIN tags t ON t.id = pt.tag_id
            WHERE pt.post_id IN ( %s )
            """.formatted(placeholders);

        jdbc.query(sql, postIds.toArray(), rs -> {
            long postId = rs.getLong("post_id");
            String tagName = rs.getString("name");

            Post post = postIdToPost.get(postId);
            if (post != null) {
                if (post.getTags() == null) {
                    post.setTags(new ArrayList<>());
                }
                post.getTags().add(tagName);
            }
        });
    }

    private Integer getTotalCount(List<String> whereParts, List<Object> args) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM posts ");

        if (!whereParts.isEmpty()) {
            sql.append("WHERE ").append(String.join(" AND ", whereParts));
        }

        return jdbc.queryForObject(sql.toString(), args.toArray(), Integer.class);
    }

    private SearchCriteria parseSearch(String raw) {
        if (raw == null || raw.isBlank())
            return new SearchCriteria(null, List.of());

        String[] parts = raw.trim().split("\\s+");
        List<String> words = new ArrayList<>();
        List<String> tags = new ArrayList<>();

        for (String part : parts) {
            if (part.isBlank())
                continue;

            if (part.startsWith("#") && part.length() > 1) {
                tags.add(part.substring(1).toLowerCase());
            } else {
                words.add(part);
            }
        }

        String titleQuery = words.isEmpty() ? null : String.join(" ", words);
        return new SearchCriteria(titleQuery, tags);
    }
}
