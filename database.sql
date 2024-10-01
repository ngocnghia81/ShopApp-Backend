CREATE DATABASE shopapp;
--Khách hàng khi muốn mua hàng => phải đăng ký tài khoản => bảng users
CREATE TABLE users
(
    id                  SERIAL PRIMARY KEY,
    fullname            VARCHAR(100)          DEFAULT '',
    phone_number        VARCHAR(10)  NOT NULL,
    address             VARCHAR(200)          DEFAULT '',
    password            VARCHAR(100) NOT NULL DEFAULT '',
    created_at          TIMESTAMP,
    updated_at          TIMESTAMP,
    is_active           BOOLEAN               DEFAULT TRUE,
    date_of_birth       DATE,
    facebook_account_id INT                   DEFAULT 0,
    google_account_id   INT                   DEFAULT 0,
    role_id             INT
);

CREATE TABLE roles
(
    id   SERIAL PRIMARY KEY,
    name VARCHAR(20) NOT NULL
);

ALTER TABLE users
    ADD CONSTRAINT fk_users_roles FOREIGN KEY (role_id) REFERENCES roles (id);

CREATE TABLE tokens
(
    id              SERIAL PRIMARY KEY,
    token           VARCHAR(255) UNIQUE NOT NULL,
    token_type      VARCHAR(50)         NOT NULL,
    expiration_date TIMESTAMP,
    revoked         BOOLEAN             NOT NULL,
    expired         BOOLEAN             NOT NULL,
    user_id         INT,
    CONSTRAINT fk_tokens_users FOREIGN KEY (user_id) REFERENCES users (id)
);

--hỗ trợ đăng nhập từ Facebook và Google
CREATE TABLE social_accounts
(
    id          SERIAL PRIMARY KEY,
    provider    VARCHAR(20)  NOT NULL,
    provider_id VARCHAR(50)  NOT NULL,
    email       VARCHAR(150) NOT NULL,
    name        VARCHAR(100) NOT NULL,
    user_id     INT,
    CONSTRAINT fk_social_accounts_users FOREIGN KEY (user_id) REFERENCES users (id)
);

--Bảng danh mục sản phẩm(Category)
CREATE TABLE categories
(
    id   SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL DEFAULT ''
);

--Bảng chứa sản phẩm(Product): "laptop macbook air 15 inch 2023", iphone 15 pro,...
CREATE TABLE products
(
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(350),
    price       FLOAT NOT NULL CHECK (price >= 0),
    thumbnail   VARCHAR(300) DEFAULT '',
    description TEXT         DEFAULT '',
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP,
    category_id INT,
    CONSTRAINT fk_products_categories FOREIGN KEY (category_id) REFERENCES categories (id)
);

CREATE TABLE product_images
(
    id         SERIAL PRIMARY KEY,
    product_id INT,
    CONSTRAINT fk_product_images_products FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE,
    image_url  VARCHAR(300)
);

--Đặt hàng - orders
CREATE TABLE orders
(
    id               SERIAL PRIMARY KEY,
    user_id          INT,
    CONSTRAINT fk_orders_users FOREIGN KEY (user_id) REFERENCES users (id),
    fullname         VARCHAR(100) DEFAULT '',
    email            VARCHAR(100) DEFAULT '',
    phone_number     VARCHAR(20)  NOT NULL,
    address          VARCHAR(200) NOT NULL,
    note             VARCHAR(100) DEFAULT '',
    order_date       TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    status           VARCHAR(20),
    total_money      FLOAT CHECK (total_money >= 0),
    shipping_method  VARCHAR(100),
    shipping_address VARCHAR(200),
    shipping_date    DATE,
    tracking_number  VARCHAR(100),
    payment_method   VARCHAR(100),
    active           BOOLEAN
);
--Trạng thái đơn hàng chỉ đc phép nhận "một số giá trị cụ thể"
-- Step 1: Create the new enum type
CREATE TYPE order_status AS ENUM ('pending', 'processing', 'shipped', 'delivered', 'cancelled');

-- Step 2: Add a temporary column with the new enum type
ALTER TABLE orders
    ADD COLUMN temp_status order_status;

-- Step 3: Copy data from the old status column to the temporary column
UPDATE orders
SET temp_status = status::order_status;

-- Step 4: Drop the old status column
ALTER TABLE orders
    DROP COLUMN status;

-- Step 5: Rename the temporary column to status
ALTER TABLE orders
    RENAME COLUMN temp_status TO status;

CREATE CAST (varchar AS order_status) WITH INOUT AS IMPLICIT;

CREATE TABLE order_details
(
    id                 SERIAL PRIMARY KEY,
    order_id           INT,
    CONSTRAINT fk_order_details_orders FOREIGN KEY (order_id) REFERENCES orders (id),
    product_id         INT,
    CONSTRAINT fk_order_details_products FOREIGN KEY (product_id) REFERENCES products (id),
    price              FLOAT CHECK (price >= 0),
    number_of_products INT CHECK (number_of_products > 0),
    total_money        FLOAT CHECK (total_money >= 0),
    color              VARCHAR(20) DEFAULT ''
);
