-- Migration: V22__create_table_system_values.sql
-- Description: Tạo bảng cấu hình hệ thống system_values và lưu cấu hình Cron mặc định

-- 1. Tạo bảng system_values
CREATE TABLE system_values
(
    id           SERIAL PRIMARY KEY,
    config_key   VARCHAR(100) NOT NULL UNIQUE,
    config_value TEXT         NOT NULL,
    description  TEXT,
    created_at   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 2. Tạo index cho config_key để tối ưu tốc độ tìm kiếm (SELECT) từ ứng dụng
CREATE INDEX idx_system_values_key ON system_values (config_key);

-- 3. Insert sẵn giá trị Cron của bạn vào hệ thống khi chạy migration
INSERT INTO system_values (config_key, config_value, description)
VALUES ('cron.schedule.research_feed.sync_daily',
        '0 0 2 * * *',
        'Schedule for daily research feed synchronization job (Every day at 02:00 AM)'),
       ('cron.schedule.statistic_weekly.run_job',
        '0 30 0 * * MON',
        'Schedule for weekly statistics calculation job (Every Monday at 00:30 AM)');

DROP TABLE IF EXISTS api_call_job CASCADE;
DROP TABLE IF EXISTS fields CASCADE;
DROP TABLE IF EXISTS subfields CASCADE;
DROP TABLE IF EXISTS taxonomy_topics CASCADE;