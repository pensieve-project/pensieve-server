CREATE KEYSPACE IF NOT EXISTS pensieve WITH REPLICATION = {'class' : 'SimpleStrategy', 'replication_factor' : 1};

USE pensieve;

CREATE TABLE IF NOT EXISTS posts (
    threadId UUID,
    authorId UUID,
    postId UUID,
    text TEXT,
    timeStamp TIMESTAMP,
    likesCount INT,
    PRIMARY KEY ((threadId), authorId, postId)
);

CREATE TABLE IF NOT EXISTS threads (
    threadId UUID,
    authorId UUID,
    title TEXT,
    timeStamp TIMESTAMP,
    PRIMARY KEY ((threadId), authorId)
);

CREATE TABLE IF NOT EXISTS profiles (
    authorId UUID,
    description TEXT,
    PRIMARY KEY ((authorId))
);

CREATE MATERIALIZED VIEW IF NOT EXISTS posts_by_author AS
SELECT threadId, authorId, postId, text, timeStamp, likesCount
FROM posts
WHERE authorId IS NOT NULL AND threadId IS NOT NULL AND postId IS NOT NULL
PRIMARY KEY ((authorId), threadId, postId);

INSERT INTO threads (threadId, authorId, title, timeStamp) values (a1b4720b-9671-4f8e-abac-2c75e37da506, d644e781-f3f7-4cee-8b41-a9ec4430cf23, 'Мой любимый питомец', '2024-03-02 15:30:00');