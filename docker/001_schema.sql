------------------------------------------------------------
-- SCHEMA: POSTS, COMMENTS, TAGS, POST_TAGS
------------------------------------------------------------

-- POSTS
CREATE TABLE if NOT EXISTS posts (
    id          BIGSERIAL PRIMARY KEY,
    title       VARCHAR(256) NOT NULL,
    text        VARCHAR(256) NOT NULL,
    likes_count INTEGER      NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- COMMENTS
CREATE TABLE IF NOT EXISTS comments (
    id         BIGSERIAL PRIMARY KEY,
    post_id    BIGINT       NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    text       VARCHAR(256) NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- TAGS
CREATE TABLE IF NOT EXISTS tags (
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(256) NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- POST_TAGS (many-to-many)
CREATE TABLE IF NOT EXISTS post_tags (
    post_id    BIGINT NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    tag_id     BIGINT NOT NULL REFERENCES tags(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (post_id, tag_id)
);

------------------------------------------------------------
-- INDEXES
------------------------------------------------------------

CREATE UNIQUE INDEX IF NOT EXISTS ux_tags_name_lower
    ON tags (LOWER(name));

CREATE INDEX IF NOT EXISTS idx_post_tags_tag_id_post_id
    ON post_tags (tag_id, post_id);

CREATE INDEX IF NOT EXISTS idx_post_tags_post_id_tag_id
    ON post_tags(post_id, tag_id);

CREATE INDEX IF NOT EXISTS idx_posts_created_at_desc
    ON posts (created_at DESC);
