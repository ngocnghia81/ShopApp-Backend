-- Đảm bảo tên bảng và cột không dùng dấu nháy đơn
ALTER TABLE categories
ALTER COLUMN name TYPE VARCHAR(50),
    ADD CONSTRAINT unique_name UNIQUE(name);