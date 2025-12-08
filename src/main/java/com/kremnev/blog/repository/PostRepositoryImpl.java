package com.kremnev.blog.repository;

import com.kremnev.blog.model.Post;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.util.Pair;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class PostRepositoryImpl implements PostRepository {

    private final NamedParameterJdbcTemplate namedJdbc;
    private final SimpleJdbcInsert postInsert;
    private final SimpleJdbcInsert tagInsert;
    private final SimpleJdbcInsert postTagInsert;

    public PostRepositoryImpl(DataSource dataSource) {
        this.namedJdbc = new NamedParameterJdbcTemplate(dataSource);
        this.postInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("posts")
                .usingGeneratedKeyColumns("id")
                .usingColumns("title", "text", "likes_count");
        this.tagInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("tags")
                .usingGeneratedKeyColumns("id")
                .usingColumns("name");
        this.postTagInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("post_tags")
                .usingColumns("post_id", "tag_id");
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

        MapSqlParameterSource params = new MapSqlParameterSource();

        if (filterByTags) {
            sql.append("JOIN post_tags pt ON pt.post_id = p.id ")
                    .append("JOIN tags t ON t.id = pt.tag_id ");
        }

        List<String> whereParts = new ArrayList<>();

        if (filterByTitle) {
            whereParts.add("LOWER(p.title) LIKE LOWER(:titleQuery)");
            params.addValue("titleQuery", "%" + sc.titleQuery + "%");
        }

        if (filterByTags) {
            whereParts.add("LOWER(t.name) IN (:tags)");
            params.addValue("tags", sc.tags);
        }

        if (!whereParts.isEmpty()) {
            sql.append("WHERE ")
                    .append(String.join(" AND ", whereParts))
                    .append(" ");
        }

        sql.append("GROUP BY p.id ");

        if (filterByTags) {
            sql.append("HAVING COUNT(DISTINCT LOWER(t.name)) = :tagCount ");
            params.addValue("tagCount", sc.tags.size());
        }

        sql.append("ORDER BY p.created_at DESC ");

        int totalCount = getTotalCount(sc);
        int offset = Math.max(pageNumber - 1, 0) * pageSize;
        sql.append("LIMIT :pageSize OFFSET :offset");
        params.addValue("pageSize", pageSize);
        params.addValue("offset", offset);

        List<Post> posts = namedJdbc.query(sql.toString(), params, new PostRowMapper());
        attachTags(posts);

        return Pair.of(posts, totalCount);
    }

    @Override
    public Optional<Post> findById(long postId) {
        try {
            String sql = """
                SELECT p.id, p.title, p.text, p.likes_count, COUNT(c.id) as comments_count,
                       p.created_at, p.updated_at
                FROM posts p
                LEFT JOIN comments c ON c.post_id = p.id
                WHERE p.id = :postId
                GROUP BY p.id
                """;

            MapSqlParameterSource params = new MapSqlParameterSource("postId", postId);
            var post = namedJdbc.queryForObject(sql, params, new PostRowMapper());

            if (post == null)
                return Optional.empty();

            attachTags(post);
            return Optional.of(post);

        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    @Transactional
    public Post create(String title, String text, List<String> tags) {
        Map<String, Object> params = Map.of(
                "title", title,
                "text", text,
                "likes_count", 0
        );

        Number postId = postInsert.executeAndReturnKey(params);
        saveTags(postId.longValue(), tags);
        return findById(postId.longValue()).orElseThrow();
    }

    @Override
    public Optional<Post> update(Long postId, String title, String text, List<String> tags) {
        String sql = "UPDATE posts SET title = :title, text = :text, updated_at = NOW() WHERE id = :postId";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("title", title)
                .addValue("text", text)
                .addValue("postId", postId);

        int rows = namedJdbc.update(sql, params);

        if (rows == 0) return Optional.empty();

        namedJdbc.update("DELETE FROM post_tags WHERE post_id = :postId",
                new MapSqlParameterSource("postId", postId));
        saveTags(postId, tags);

        return findById(postId);
    }

    @Override
    public boolean delete(Long postId) {
        String sql = "DELETE FROM posts WHERE id = :postId";
        int rows = namedJdbc.update(sql, new MapSqlParameterSource("postId", postId));
        return rows > 0;
    }

    @Override
    public Optional<Post> addLike(Long postId) {
        String sql = "UPDATE posts SET likes_count = likes_count + 1, updated_at = NOW() WHERE id = :postId";
        int rows = namedJdbc.update(sql, new MapSqlParameterSource("postId", postId));

        if (rows == 0) {
            return Optional.empty();
        }

        return findById(postId);
    }

    private Long getOrCreateTagId(String tagName) {
        String normalized = tagName.trim().toLowerCase();
        if (normalized.isBlank()) return null;

        String sql = "SELECT id FROM tags WHERE LOWER(name) = :name";
        List<Long> existing = namedJdbc.query(sql,
                new MapSqlParameterSource("name", normalized),
                (rs, rowNum) -> rs.getLong("id"));

        if (!existing.isEmpty())
            return existing.get(0);

        Map<String, Object> params = Map.of("name", normalized);
        Number tagId = tagInsert.executeAndReturnKey(params);
        return tagId.longValue();
    }

    private void saveTags(long postId, List<String> tags) {
        List<String> normalizedTags = normalizeTags(tags);
        for (String tagName : normalizedTags) {
            Long tagId = getOrCreateTagId(tagName);
            if (tagId == null) continue;

            String checkSql = "SELECT COUNT(*) FROM post_tags WHERE post_id = :postId AND tag_id = :tagId";
            MapSqlParameterSource checkParams = new MapSqlParameterSource()
                    .addValue("postId", postId)
                    .addValue("tagId", tagId);

            Integer count = namedJdbc.queryForObject(checkSql, checkParams, Integer.class);

            if (count == null || count == 0) {
                Map<String, Object> params = Map.of(
                        "post_id", postId,
                        "tag_id", tagId
                );
                postTagInsert.execute(params);
            }
        }
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

        String sql = """
            SELECT pt.post_id, t.name
            FROM post_tags pt
            JOIN tags t ON t.id = pt.tag_id
            WHERE pt.post_id IN (:postIds)
            """;

        MapSqlParameterSource params = new MapSqlParameterSource("postIds", postIds);

        namedJdbc.query(sql, params, rs -> {
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

    private Integer getTotalCount(SearchCriteria sc) {
        boolean filterByTags = !sc.tags.isEmpty();
        boolean filterByTitle = sc.titleQuery != null && !sc.titleQuery.isBlank();

        MapSqlParameterSource params = new MapSqlParameterSource();
        List<String> whereParts = new ArrayList<>();

        if (filterByTitle) {
            whereParts.add("LOWER(p.title) LIKE LOWER(:titleQuery)");
            params.addValue("titleQuery", "%" + sc.titleQuery + "%");
        }

        if (filterByTags) {
            whereParts.add("LOWER(t.name) IN (:tags)");
            params.addValue("tags", sc.tags);
        }

        // For simple cases (no tags or single tag), use simple COUNT
        if (!filterByTags || sc.tags.size() <= 1) {
            StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM posts p");

            if (filterByTags) {
                sql.append(" JOIN post_tags pt ON pt.post_id = p.id ")
                   .append("JOIN tags t ON t.id = pt.tag_id");
            }

            if (!whereParts.isEmpty()) {
                sql.append(" WHERE ").append(String.join(" AND ", whereParts));
            }

            return namedJdbc.queryForObject(sql.toString(), params, Integer.class);
        }

        // For multiple tags with AND logic, we need to count distinct posts
        StringBuilder sql = new StringBuilder(
            "SELECT COUNT(DISTINCT p.id) FROM posts p " +
            "JOIN post_tags pt ON pt.post_id = p.id " +
            "JOIN tags t ON t.id = pt.tag_id"
        );

        if (!whereParts.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", whereParts));
        }

        sql.append(" GROUP BY p.id HAVING COUNT(DISTINCT LOWER(t.name)) = :tagCount");
        params.addValue("tagCount", sc.tags.size());

        // Wrap in outer query to count the grouped results
        String countSql = "SELECT COUNT(*) FROM (" + sql.toString() + ") AS counted_posts";
        return namedJdbc.queryForObject(countSql, params, Integer.class);
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

    private List<String> normalizeTags(List<String> tags) {
        return tags == null ? List.of() :
                tags.stream()
                        .filter(t -> t != null && !t.isBlank())
                        .map(t -> t.trim().toLowerCase())
                        .distinct()
                        .toList();
    }
}
