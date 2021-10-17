CREATE TYPE ORDER_STATUS AS ENUM ('NOT_COMPLETE', 'ORDERED', 'ASSIGNED', 'DELIVERED');
CREATE TYPE PRODUCT_STATUS AS ENUM ('IN_PROCESSING', 'AVAILABLE', 'NOT_AVAILABLE');



CREATE TABLE IF NOT EXISTS roles (
uuid UUID PRIMARY KEY,
type VARCHAR(45)  NOT NULL
);

CREATE TABLE IF NOT EXISTS users (
uuid UUID PRIMARY KEY,
name VARCHAR(45) UNIQUE NOT NULL,
password VARCHAR(100) NOT NULL,
role_id UUID NOT NULL,
CONSTRAINT role_fkey FOREIGN KEY (role_id)
REFERENCES roles (uuid)
);

CREATE TABLE IF NOT EXISTS orders(
uuid UUID PRIMARY KEY,
user_id UUID NOT NULL,
status ORDER_STATUS DEFAULT  'NOT_COMPLETE',
CONSTRAINT user_fkey FOREIGN KEY (user_id)
    REFERENCES users (uuid));

CREATE TABLE IF NOT EXISTS courier_info (
uuid UUID PRIMARY KEY,
name VARCHAR(45) NOT NULL,
phone_number VARCHAR(20) NOT NULL,
user_id UUID UNIQUE NOT NULL,
CONSTRAINT user_fkey FOREIGN KEY (user_id)
    REFERENCES users (uuid));



CREATE TABLE IF NOT EXISTS suppliers (
     uuid UUID PRIMARY KEY,
     name VARCHAR(45) UNIQUE NOT NULL
    );


CREATE TABLE IF NOT EXISTS categories (
     uuid UUID PRIMARY KEY,
     name VARCHAR(45) UNIQUE NOT NULL
    );


CREATE TABLE IF NOT EXISTS products (
uuid UUID PRIMARY KEY,
name VARCHAR(45) NOT NULL,
description VARCHAR(45) NOT NULL,
cost NUMERIC NOT NULL,
publication_date VARCHAR(45) NOT NULL,
status PRODUCT_STATUS NOT NULL,
supplier_id UUID NOT NULL,
category_id UUID NOT NULL,
CONSTRAINT supplier_fkey FOREIGN KEY (supplier_id)
REFERENCES suppliers (uuid),
CONSTRAINT category_fkey FOREIGN KEY (category_id)
REFERENCES categories (uuid)
);


CREATE TABLE IF NOT EXISTS orders_products(
    order_id UUID NOT NULL,
    product_id UUID NOT NULL,
    PRIMARY KEY (order_id, product_id),
    FOREIGN KEY (order_id) REFERENCES orders(uuid),
    FOREIGN KEY (product_id) REFERENCES products (uuid)
);

CREATE TABLE IF NOT EXISTS orders_have_courier_info(
    order_id UUID NOT NULL,
    courier_info_id UUID NOT NULL,
    PRIMARY KEY (order_id, courier_info_id),
    FOREIGN KEY (order_id) REFERENCES orders(uuid),
    FOREIGN KEY (courier_info_id) REFERENCES courier_info(uuid)
);


CREATE TABLE IF NOT EXISTS specific_products(
    user_id UUID NOT NULL,
    product_id UUID NOT NULL,
    PRIMARY KEY (user_id, product_id),
    FOREIGN KEY (user_id) REFERENCES users(uuid),
    FOREIGN KEY (product_id) REFERENCES products(uuid)
);


CREATE TABLE IF NOT EXISTS user_category_notifications(
    user_id UUID NOT NULL,
    category_id UUID NOT NULL,
    PRIMARY KEY (user_id, category_id),
    FOREIGN KEY (user_id) REFERENCES users (uuid),
    FOREIGN KEY (category_id) REFERENCES categories (uuid)
);

CREATE TABLE IF NOT EXISTS user_supplier_notifications (
    user_id UUID NOT NULL,
    supplier_id UUID NOT NULL,
    PRIMARY KEY (user_id, supplier_id),
    CONSTRAINT user_fkey FOREIGN KEY (user_id)
    REFERENCES users (uuid),
    CONSTRAINT supplier_fkey FOREIGN KEY (supplier_id)
    REFERENCES suppliers (uuid)
);



INSERT INTO roles VALUES
    ('123e4567-e89b-12d3-a456-556642440000', 'Admin')


