DO $$
BEGIN
   IF NOT EXISTS (SELECT FROM pg_database WHERE datname = 'pensieveDatabase') THEN
      CREATE DATABASE "pensieveDatabase";
   END IF;
END $$;

\c pensieveDatabase;

CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    passwordHash VARCHAR(255) NOT NULL,
    salt VARCHAR(255) NOT NULL,
    refreshToken VARCHAR(255)
);
