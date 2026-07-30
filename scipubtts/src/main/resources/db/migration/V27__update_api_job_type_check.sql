-- V27__update_api_job_type_check.sql

-- 1. Xóa check constraint hiện tại
ALTER TABLE api_job
DROP CONSTRAINT IF EXISTS chk_api_job_type;

-- 2. Thêm lại check constraint chứa TẤT CẢ các job_type trong enum Java của bạn
ALTER TABLE api_job
    ADD CONSTRAINT chk_api_job_type
        CHECK (job_type IN ('JOURNAL_DAILY_SYNC', 'DASHBOARD_SYNC', 'FEED_SYNC'));