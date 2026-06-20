-- Submission file column rename: file_url -> file_key.
-- Reason: storage moved from local disk (where the column held a public URL) to
-- MinIO's private bucket (where the column holds the object key, and a presigned
-- URL is generated on demand). Hibernate `ddl-auto=validate` rejects the old
-- column name once the entity is renamed, so this migration must run.
-- Written idempotently so re-runs on an already-migrated DB are safe.

do $$
begin
    if exists (
        select 1 from information_schema.columns
        where table_name = 'submission' and column_name = 'file_url'
    ) and not exists (
        select 1 from information_schema.columns
        where table_name = 'submission' and column_name = 'file_key'
    ) then
        alter table submission rename column file_url to file_key;
    end if;

    if not exists (
        select 1 from information_schema.columns
        where table_name = 'submission' and column_name = 'file_key'
    ) then
        alter table submission add column file_key varchar(255) not null default '';
        alter table submission alter column file_key drop default;
    end if;
end $$;
