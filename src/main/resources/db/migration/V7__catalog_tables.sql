CREATE TABLE IF NOT EXISTS catalog_universities (
    id   VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    logo VARCHAR(1024)
);

CREATE TABLE IF NOT EXISTS catalog_careers (
    id            VARCHAR(255) PRIMARY KEY,
    name          VARCHAR(255) NOT NULL,
    university_id VARCHAR(255) NOT NULL REFERENCES catalog_universities(id)
);

CREATE TABLE IF NOT EXISTS catalog_subjects (
    id   VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    icon VARCHAR(1024)
);

CREATE INDEX IF NOT EXISTS idx_catalog_careers_university_id ON catalog_careers(university_id);
