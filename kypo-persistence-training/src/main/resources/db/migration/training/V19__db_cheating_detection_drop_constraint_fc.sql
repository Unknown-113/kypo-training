ALTER TABLE forbidden_command DROP CONSTRAINT cheating_detection_id;
ALTER TABLE forbidden_command ALTER COLUMN cheating_detection_id DROP NOT NULL;