CREATE KEYSPACE IF NOT EXISTS pensieve WITH REPLICATION = {'class' : 'SimpleStrategy', 'replication_factor' : 1};

USE pensieve;

CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY,
    username TEXT,
    email TEXT
);

INSERT INTO users (id, username, email) VALUES (uuid(), 'Alice', 'alice@gmail.com');
INSERT INTO users (id, username, email) VALUES (uuid(), 'Bob', 'bob@gmail.com');
