DO $$
BEGIN
    -- Kiểm tra xem cột 'is_mobile' có tồn tại không
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'tokens'
        AND column_name = 'refresh_token'
    ) THEN
        -- Nếu cột không tồn tại, thêm cột vào bảng 'tokens'
        EXECUTE 'ALTER TABLE tokens ADD COLUMN refresh_token VARCHAR(255)';
END IF;

    -- Kiểm tra xem cột 'refresh_expiration_date' có tồn tại không
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'tokens'
        AND column_name = 'refresh_expiration_date'
    ) THEN
        -- Nếu cột không tồn tại, thêm cột vào bảng 'tokens'
        EXECUTE 'ALTER TABLE tokens ADD COLUMN refresh_expiration_date TIMESTAMP';
END IF;
END $$;
