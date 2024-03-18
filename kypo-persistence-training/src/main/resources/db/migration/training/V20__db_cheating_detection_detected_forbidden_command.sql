alter table detected_forbidden_command add column hostname varchar(255);
alter table detected_forbidden_command add column occurred_at timestamp;