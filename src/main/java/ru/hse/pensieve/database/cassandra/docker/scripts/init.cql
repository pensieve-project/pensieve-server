CREATE KEYSPACE IF NOT EXISTS pensieve WITH REPLICATION = {'class' : 'SimpleStrategy', 'replication_factor' : 1};

USE pensieve;

CREATE TABLE IF NOT EXISTS posts (
    themeId UUID,
    authorId UUID,
    postId UUID,
    photo BLOB,
    text TEXT,
    timeStamp TIMESTAMP,
    likesCount INT,
    commentsCount INT,
    PRIMARY KEY ((themeId), authorId, postId)
);

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

CREATE MATERIALIZED VIEW IF NOT EXISTS posts_by_author AS
SELECT * FROM posts
WHERE authorId IS NOT NULL AND themeId IS NOT NULL AND postId IS NOT NULL
PRIMARY KEY ((authorId), themeId, postId);

CREATE MATERIALIZED VIEW IF NOT EXISTS posts_by_id AS
SELECT * FROM posts
WHERE authorId IS NOT NULL AND themeId IS NOT NULL AND postId IS NOT NULL
PRIMARY KEY ((postId), authorId, themeId);