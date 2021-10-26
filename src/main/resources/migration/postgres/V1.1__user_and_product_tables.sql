CREATE TYPE ORDER_STATUS AS ENUM ('NOT_COMPLETE', 'ORDERED', 'ASSIGNED', 'DELIVERED');
CREATE TYPE PRODUCT_STATUS AS ENUM ('IN_PROCESSING', 'AVAILABLE', 'NOT_AVAILABLE');
CREATE TYPE USER_ROLE AS ENUM ('MANAGER', 'CLIENT', 'COURIER');


CREATE TABLE IF NOT EXISTS users (
    uuid UUID PRIMARY KEY,
    name VARCHAR(45) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    mail VARCHAR(100) NOT NULL,
    role USER_ROLE NOT NULL
);

CREATE TABLE IF NOT EXISTS courier_info (
    uuid UUID NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    PRIMARY KEY(uuid),
    CONSTRAINT user_fkey FOREIGN KEY (uuid)
    REFERENCES users (uuid)
);


CREATE TABLE IF NOT EXISTS orders(
    uuid UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    courier_info_id UUID NOT NULL,
    user_address VARCHAR(100) NOT NULL,
    status ORDER_STATUS DEFAULT  'NOT_COMPLETE',
    CONSTRAINT user_fkey FOREIGN KEY (user_id)
    REFERENCES users (uuid),
    CONSTRAINT courier_info_fkey FOREIGN KEY (courier_info_id)
    REFERENCES courier_info (uuid)
);


CREATE TABLE IF NOT EXISTS suppliers (
     uuid UUID PRIMARY KEY,
     name VARCHAR(45) UNIQUE NOT NULL
);


CREATE TABLE IF NOT EXISTS categories (
     uuid UUID PRIMARY KEY,
     name VARCHAR(45) UNIQUE NOT NULL,
     description VARCHAR(255) UNIQUE NOT NULL
);


CREATE TABLE IF NOT EXISTS products (
uuid UUID PRIMARY KEY,
name VARCHAR(45) NOT NULL,
description VARCHAR(255) NOT NULL,
cost VARCHAR(45) NOT NULL,
publication_date VARCHAR(45) NOT NULL,
status PRODUCT_STATUS NOT NULL,
supplier_id UUID NOT NULL,
CONSTRAINT supplier_fkey FOREIGN KEY (supplier_id)
REFERENCES suppliers (uuid)
);

CREATE TABLE IF NOT EXISTS product_categories(
    category_id UUID NOT NULL,
    product_id UUID NOT NULL,
    PRIMARY KEY (category_id, product_id),
    FOREIGN KEY (category_id) REFERENCES categories(uuid),
    FOREIGN KEY (product_id) REFERENCES products (uuid)
);

CREATE TABLE IF NOT EXISTS orders_products(
    order_id UUID NOT NULL,
    product_id UUID NOT NULL,
    quantity INT NOT NULL,
    PRIMARY KEY (order_id, product_id),
    FOREIGN KEY (order_id) REFERENCES orders(uuid),
    FOREIGN KEY (product_id) REFERENCES products (uuid)
);


CREATE TABLE IF NOT EXISTS user_groups(
    uuid UUID PRIMARY KEY,
    name VARCHAR(45) NOT NULL
);

CREATE TABLE IF NOT EXISTS groups_and_users(
    user_group_id UUID NOT NULL,
    user_id UUID NOT NULL,
    PRIMARY KEY (user_group_id, user_id),
    FOREIGN KEY (user_group_id) REFERENCES user_groups(uuid),
    FOREIGN KEY (user_id) REFERENCES users(uuid)
);

CREATE TABLE IF NOT EXISTS specific_products(
    user_group_id UUID NOT NULL,
    product_id UUID NOT NULL,
    PRIMARY KEY (user_group_id, product_id),
    FOREIGN KEY (user_group_id) REFERENCES user_groups(uuid),
    FOREIGN KEY (product_id) REFERENCES products(uuid)
);




CREATE TABLE IF NOT EXISTS user_category_subscriptions(
    user_id UUID NOT NULL,
    category_id UUID NOT NULL,
    PRIMARY KEY (user_id, category_id),
    FOREIGN KEY (user_id) REFERENCES users (uuid),
    FOREIGN KEY (category_id) REFERENCES categories (uuid)
);

CREATE TABLE IF NOT EXISTS user_supplier_ubscriptions (
    user_id UUID NOT NULL,
    supplier_id UUID NOT NULL,
    PRIMARY KEY (user_id, supplier_id),
    CONSTRAINT user_fkey FOREIGN KEY (user_id)
    REFERENCES users (uuid),
    CONSTRAINT supplier_fkey FOREIGN KEY (supplier_id)
    REFERENCES suppliers (uuid)
);


CREATE TABLE IF NOT EXISTS imagePath (
    uuid UUID NOT NULL,
    product_id UUID NOT NULL,
    FOREIGN KEY (product_id) REFERENCES products(uuid)
);


INSERT INTO users VALUES
    ('123e4567-e89b-12d3-a456-556642440000', 'Vlad', '123q', 'vlad@mail.ru', 'MANAGER')


