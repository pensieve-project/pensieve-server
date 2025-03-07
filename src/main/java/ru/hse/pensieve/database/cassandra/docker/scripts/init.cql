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
    PRIMARY KEY ((themeId), authorId, postId)
);

CREATE TABLE IF NOT EXISTS themes (
    themeId UUID,
    authorId UUID,
    title TEXT,
    timeStamp TIMESTAMP,
    PRIMARY KEY ((themeId), authorId)
);

CREATE TABLE IF NOT EXISTS profiles (
    authorId UUID,
    description TEXT,
    PRIMARY KEY ((authorId))
);

CREATE MATERIALIZED VIEW IF NOT EXISTS posts_by_author AS
SELECT themeId, authorId, postId, photo, text, timeStamp, likesCount
FROM posts
WHERE authorId IS NOT NULL AND themeId IS NOT NULL AND postId IS NOT NULL
PRIMARY KEY ((authorId), themeId, postId);

INSERT INTO themes (themeId, authorId, title, timeStamp) values (a1b4720b-9671-4f8e-abac-2c75e37da506, d644e781-f3f7-4cee-8b41-a9ec4430cf23, 'Мой любимый питомец', '2024-03-02 15:30:00');