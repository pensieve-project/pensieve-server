CREATE KEYSPACE IF NOT EXISTS pensieve WITH REPLICATION = {'class' : 'SimpleStrategy', 'replication_factor' : 1};

USE pensieve;

CREATE TYPE IF NOT EXISTS point_type (
    latitude double,
    longitude double
);

CREATE TABLE IF NOT EXISTS posts (
    themeId UUID,
    authorId UUID,
    postId UUID,
    photo BLOB,
    text TEXT,
    timeStamp TIMESTAMP,
    location frozen<point_type>,
    coAuthors FROZEN<SET<UUID>>,
    likesCount INT,
    commentsCount INT,
    PRIMARY KEY ((themeId), timeStamp, authorId, postId)
) WITH CLUSTERING ORDER BY (timeStamp DESC);

CREATE TABLE IF NOT EXISTS themes (
    themeId UUID,
    authorId UUID,
    title TEXT,
    timeStamp TIMESTAMP,
    PRIMARY KEY ((themeId))
);

CREATE TABLE IF NOT EXISTS profiles (
    authorId UUID,
    avatar BLOB,
    description TEXT,
    likedThemesIds LIST<UUID>,
    likedPostsIds LIST<UUID>,
    subscriptionsCount INT,
    subscribersCount INT,
    isVip BOOLEAN,
    PRIMARY KEY ((authorId))
);

CREATE TABLE IF NOT EXISTS comments (
    postId UUID,
    commentId TIMEUUID,
    authorId UUID,
    text TEXT,
    PRIMARY KEY ((postId), commentId)
);

CREATE TABLE IF NOT EXISTS subscriptions_by_subscriber (
    subscriberId UUID,
    targetId UUID,
    timeStamp TIMESTAMP,
    PRIMARY KEY (subscriberId, targetId)
);

CREATE TABLE IF NOT EXISTS subscribers_by_target (
    targetId UUID,
    subscriberId UUID,
    timeStamp TIMESTAMP,
    PRIMARY KEY (targetId, subscriberId)
);

CREATE TABLE IF NOT EXISTS user_feed (
    userId UUID,
    bucket INT,
    themeId UUID,
    postId UUID,
    authorId UUID,
    photo BLOB,
    text TEXT,
    timeStamp TIMESTAMP,
    location frozen<point_type>,
    coAuthors FROZEN<SET<UUID>>,
    likesCount INT,
    commentsCount INT,
    PRIMARY KEY ((userId, bucket), timeStamp, postId)
) WITH CLUSTERING ORDER BY (timeStamp DESC);

CREATE TABLE IF NOT EXISTS albums (
    userId UUID,
    coAuthors FROZEN<SET<UUID>>,
    timeStamp TIMESTAMP,
    PRIMARY KEY ((userId), coAuthors)
);

CREATE MATERIALIZED VIEW IF NOT EXISTS posts_by_author AS
SELECT * FROM posts
WHERE authorId IS NOT NULL AND themeId IS NOT NULL AND postId IS NOT NULL AND timeStamp IS NOT NULL
PRIMARY KEY ((authorId), timeStamp, themeId, postId)
WITH CLUSTERING ORDER BY (timeStamp DESC, themeId ASC, postId ASC);

CREATE MATERIALIZED VIEW IF NOT EXISTS posts_by_coauthors AS
SELECT * FROM posts
WHERE authorId IS NOT NULL AND coAuthors IS NOT NULL AND themeId IS NOT NULL AND postId IS NOT NULL AND timeStamp IS NOT NULL
PRIMARY KEY ((coAuthors), timeStamp, authorId, themeId, postId)
WITH CLUSTERING ORDER BY (timeStamp DESC, authorId ASC, themeId ASC, postId ASC);

CREATE MATERIALIZED VIEW IF NOT EXISTS posts_by_id AS
SELECT * FROM posts
WHERE authorId IS NOT NULL AND themeId IS NOT NULL AND postId IS NOT NULL AND timeStamp IS NOT NULL
PRIMARY KEY ((postId), timeStamp, authorId, themeId);