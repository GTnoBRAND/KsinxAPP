-- Adds created_at / updated_at to courses, populated by Hibernate's
-- @CreationTimestamp and @UpdateTimestamp. updated_at feeds the <lastmod>
-- field of CourseSitemapProjection, so existing rows are backfilled with
-- now() to give the sitemap reasonable values until the next update.
-- Idempotent so re-runs are safe.

do $$
begin
    if not exists (
        select 1 from information_schema.columns
        where table_name = 'courses' and column_name = 'updated_at'
    ) then
        alter table courses add column updated_at timestamp(6) with time zone;
        update courses set updated_at = now() where updated_at is null;
    end if;

    if not exists (
        select 1 from information_schema.columns
        where table_name = 'courses' and column_name = 'created_at'
    ) then
        alter table courses add column created_at timestamp(6);
        update courses set created_at = now() where created_at is null;
    end if;
end $$;
