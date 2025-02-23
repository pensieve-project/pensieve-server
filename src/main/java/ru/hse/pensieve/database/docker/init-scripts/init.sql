DO $$
BEGIN
   IF NOT EXISTS (SELECT FROM pg_database WHERE datname = 'pensieveDatabase') THEN
      CREATE DATABASE "pensieveDatabase";
   END IF;
END $$;

\c pensieveDatabase;

CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL
);

INSERT INTO users (username, email) VALUES
('Alice', 'alice@gmail.com'),
('Bob', 'bob@gmail.com');
