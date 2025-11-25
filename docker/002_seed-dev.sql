------------------------------------------------------------
-- SEED TAGS
------------------------------------------------------------

INSERT INTO tags(name) VALUES
  ('spring'),
  ('java'),
  ('backend'),
  ('docker'),
  ('postgres'),
  ('devops'),
  ('api'),
  ('microservices'),
  ('performance'),
  ('testing'),
  ('security'),
  ('architecture');

------------------------------------------------------------
-- SEED POSTS
------------------------------------------------------------

INSERT INTO posts(title, text, likes_count) VALUES
  ('Building a REST API with Spring', 'A practical guide on creating REST APIs using Spring MVC and JdbcTemplate.', 3),
  ('Understanding Dependency Injection', 'Explaining DI, IoC containers, and practical patterns in Java.', 5),
  ('Docker: Complete Beginner Guide', 'Learn how to containerize your Java applications.', 2),
  ('PostgreSQL Indexing Tips', 'A collection of performance tricks for optimizing SQL queries.', 7),
  ('Handling Errors in REST APIs', 'How to design meaningful error responses.', 1),
  ('Testing Strategies for Java Projects', 'Unit, integration, E2E — which to choose?', 4),
  ('Clean Architecture Explained', 'Layering, boundaries, DTOs, and why it matters.', 10),
  ('Microservices vs Monoliths', 'Pros, cons, and when to choose what.', 6),
  ('Caching Strategies 101', 'Redis, local caches, invalidation patterns.', 3),
  ('JWT Security Basics', 'Understanding tokens, refresh flows, and best practices.', 8),
  ('Database Transactions in Spring', 'Declarative vs programmatic transactions.', 5),
  ('Pagination Best Practices', 'Offset-pagination vs keyset-pagination.', 1),
  ('Versioning REST APIs', 'URI versioning vs header versioning.', 0),
  ('Async vs Sync APIs', 'When to use which, and why.', 2),
  ('Docker Compose for Local Dev', 'Multi-container setups and networking.', 4),
  ('How to Optimize SQL Queries', 'Explain analyze, indexes, and query rewriting.', 9),
  ('Java Collections Deep Dive', 'Understanding lists, sets, maps, and performance.', 6),
  ('Rate Limiting Your API', 'Token bucket, leaky bucket, and sliding window.', 5),
  ('Monitoring Microservices', 'Prometheus, Grafana, logs, metrics.', 3),
  ('Intro to CQRS', 'Command and Query Responsibility Segregation explained.', 4);

------------------------------------------------------------
-- SEED COMMENTS
------------------------------------------------------------

INSERT INTO comments(post_id, text) VALUES
  (1, 'Great explanation, thanks!'),
  (1, 'This is exactly what I needed today.'),
  (2, 'DI finally makes sense.'),
  (2, 'Could you add a section on circular dependencies?'),
  (3, 'Docker is awesome, thanks for this!'),
  (3, 'What about multi-stage builds?'),
  (4, 'Super helpful tips, my queries are much faster now.'),
  (4, 'Indexing is tricky but powerful.'),
  (5, 'Error handling is underrated.'),
  (6, 'Love the testing breakdown.'),
  (6, 'Please add JUnit5 examples!'),
  (7, 'Clean architecture changed how I code.'),
  (8, 'Good comparison of microservices vs monoliths.'),
  (8, 'Can you cover distributed transactions?'),
  (9, 'Caching saves lives.'),
  (10, 'JWT section was very helpful.'),
  (11, 'Great explanation of transactions.'),
  (12, 'Pagination tips were really good.'),
  (13, 'How to version GraphQL APIs?'),
  (14, 'Async APIs can be tricky, thanks for clarifying.'),
  (15, 'Compose is amazing for dev environments.'),
  (16, 'Explain analyze is magical.'),
  (17, 'Collections are deeper than I thought.'),
  (18, 'Rate limiting examples were great.'),
  (19, 'Monitoring is key to production success.'),
  (20, 'CQRS is mind-bending but useful.'),
  (1, 'More code examples would be great!'),
  (3, 'Any advice for using Docker in CI/CD?'),
  (10, 'Security is everything.'),
  (16, 'More optimization tricks please!');

------------------------------------------------------------
-- SEED POST_TAGS
------------------------------------------------------------

INSERT INTO post_tags(post_id, tag_id) VALUES
  (1, 1), (1, 2), (1, 7),
  (2, 2), (2, 11),
  (3, 4), (3, 6),
  (4, 5), (4, 9),
  (5, 7), (5, 11),
  (6, 10), (6, 2),
  (7, 12), (7, 2),
  (8, 8), (8, 12),
  (9, 9), (9, 7),
  (10, 11), (10, 9),
  (11, 2), (11, 5),
  (12, 7), (12, 9),
  (13, 7), (13, 12),
  (14, 7), (14, 11),
  (15, 4), (15, 6),
  (16, 5), (16, 9), (16, 12),
  (17, 2), (17, 9),
  (18, 11), (18, 9),
  (19, 6), (19, 12),
  (20, 12), (20, 11);