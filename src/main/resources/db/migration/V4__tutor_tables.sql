-- ============================================================================
-- V4 — baseline of tutor tables (auto-created by Hibernate prior to Flyway ownership)
-- All statements are idempotent: pre-existing dev DBs are left intact.
-- ============================================================================

CREATE TABLE IF NOT EXISTS tutors (
    id                  UUID              NOT NULL PRIMARY KEY,
    name                VARCHAR(255)      NOT NULL,
    subject_specialty   VARCHAR(255),
    university          VARCHAR(255),
    location            VARCHAR(255),
    modalidad           VARCHAR(255),
    rating              DOUBLE PRECISION  NOT NULL DEFAULT 0,
    reviews_count       INTEGER           NOT NULL DEFAULT 0,
    bio                 VARCHAR(2000),
    photo_url           VARCHAR(255),
    active              BOOLEAN           NOT NULL DEFAULT FALSE,
    hourly_rate         DOUBLE PRECISION  NOT NULL DEFAULT 0,
    methodology_intro   VARCHAR(1000),
    schedules_note      VARCHAR(255),
    phone_number        VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS tutor_subjects (
    tutor_id    UUID         NOT NULL REFERENCES tutors(id) ON DELETE CASCADE,
    name        VARCHAR(255),
    description VARCHAR(255),
    icon        VARCHAR(255)
);
CREATE INDEX IF NOT EXISTS idx_tutor_subjects_tutor_id ON tutor_subjects(tutor_id);

CREATE TABLE IF NOT EXISTS tutor_schedules (
    tutor_id UUID         NOT NULL REFERENCES tutors(id) ON DELETE CASCADE,
    days     VARCHAR(255),
    hours    VARCHAR(255)
);
CREATE INDEX IF NOT EXISTS idx_tutor_schedules_tutor_id ON tutor_schedules(tutor_id);

CREATE TABLE IF NOT EXISTS tutor_plans (
    tutor_id    UUID         NOT NULL REFERENCES tutors(id) ON DELETE CASCADE,
    name        VARCHAR(255),
    description VARCHAR(255),
    price       VARCHAR(255),
    unit        VARCHAR(255),
    badge       VARCHAR(255),
    featured    BOOLEAN      NOT NULL DEFAULT FALSE
);
CREATE INDEX IF NOT EXISTS idx_tutor_plans_tutor_id ON tutor_plans(tutor_id);

CREATE TABLE IF NOT EXISTS tutor_methodology_features (
    tutor_id UUID         NOT NULL REFERENCES tutors(id) ON DELETE CASCADE,
    label    VARCHAR(255),
    value    BOOLEAN      NOT NULL DEFAULT FALSE
);
CREATE INDEX IF NOT EXISTS idx_tutor_methodology_features_tutor_id ON tutor_methodology_features(tutor_id);
