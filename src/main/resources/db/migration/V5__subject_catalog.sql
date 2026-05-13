CREATE TABLE IF NOT EXISTS subjects (
    id UUID PRIMARY KEY,
    canonical_name VARCHAR(255) NOT NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS ux_subjects_canonical_name_lower
    ON subjects (LOWER(canonical_name));

CREATE TABLE IF NOT EXISTS subject_aliases (
    subject_id UUID NOT NULL REFERENCES subjects(id) ON DELETE CASCADE,
    alias VARCHAR(255) NOT NULL,
    PRIMARY KEY (subject_id, alias)
);
CREATE INDEX IF NOT EXISTS ix_subject_aliases_lower
    ON subject_aliases (LOWER(alias));
