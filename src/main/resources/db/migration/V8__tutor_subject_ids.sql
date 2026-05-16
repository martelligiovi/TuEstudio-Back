-- Drop the legacy embedded-subject table (if exists — safe for environments where it may
-- already have been dropped manually during testing).
DROP TABLE IF EXISTS tutor_subjects;

-- New join table: tutor-to-catalog-subject reference by UUID.
-- subject_id references the canonical subjects table (UUID PK) from V5.
-- ON DELETE CASCADE on tutor_id: deleting a tutor removes their subject assignments.
-- NO cascade on subject_id: admin must unassign tutors before deleting a catalog subject.
CREATE TABLE tutor_subject_ids (
    tutor_id   UUID NOT NULL REFERENCES tutors(id)   ON DELETE CASCADE,
    subject_id UUID NOT NULL REFERENCES subjects(id),
    PRIMARY KEY (tutor_id, subject_id)
);

CREATE INDEX idx_tutor_subject_ids_subject ON tutor_subject_ids(subject_id);
