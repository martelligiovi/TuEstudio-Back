CREATE TABLE IF NOT EXISTS users (
    id              UUID         NOT NULL PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    email           VARCHAR(255) NOT NULL UNIQUE,
    hashed_password VARCHAR(255) NOT NULL,
    role            VARCHAR(50)  NOT NULL
);
