DO $$
BEGIN
    -- Kiểm tra xem cột 'is_mobile' có tồn tại không
    IF NOT EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'tokens' 
        AND column_name = 'is_mobile'
    ) THEN
        -- Nếu cột không tồn tại, thêm cột vào bảng 'tokens'
        EXECUTE 'ALTER TABLE tokens ADD COLUMN is_mobile BOOLEAN DEFAULT FALSE';
END IF;
END $$;
