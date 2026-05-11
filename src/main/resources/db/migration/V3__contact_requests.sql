-- Fresh databases: create the table with the full schema.
CREATE TABLE IF NOT EXISTS contact_requests (
    id           UUID                     NOT NULL PRIMARY KEY,
    tutor_id     UUID                     NOT NULL,
    nombre       VARCHAR(255)             NOT NULL,
    telefono     VARCHAR(255)             NOT NULL,
    universidad  VARCHAR(255),
    carrera      VARCHAR(255),
    materia      VARCHAR(255),
    status       VARCHAR(50)              NOT NULL DEFAULT 'PENDING',
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Existing dev databases where Hibernate auto-created an older version of the table:
-- add the columns introduced in commit 46c9a4f with safe defaults so existing rows survive.
ALTER TABLE contact_requests ADD COLUMN IF NOT EXISTS status     VARCHAR(50)              NOT NULL DEFAULT 'PENDING';
ALTER TABLE contact_requests ADD COLUMN IF NOT EXISTS created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();
