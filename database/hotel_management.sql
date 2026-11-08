CREATE DATABASE IF NOT EXISTS hotel_management
 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE hotel_management;

CREATE TABLE roles (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 role_name VARCHAR(30) NOT NULL UNIQUE
);
CREATE TABLE users (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 username VARCHAR(50) NOT NULL UNIQUE,
 password_hash VARCHAR(100) NOT NULL,
 full_name VARCHAR(100) NOT NULL,
 role_id BIGINT NOT NULL,
 active BOOLEAN NOT NULL DEFAULT TRUE,
 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 CONSTRAINT fk_users_role FOREIGN KEY(role_id) REFERENCES roles(id)
);
CREATE TABLE customers (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 full_name VARCHAR(100) NOT NULL,
 phone VARCHAR(20) NOT NULL,
 email VARCHAR(100),
 identity_number VARCHAR(30),
 address VARCHAR(255),
 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 UNIQUE KEY uk_customer_phone(phone)
);
CREATE TABLE employees (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 user_id BIGINT NOT NULL UNIQUE,
 phone VARCHAR(20),
 position_name VARCHAR(60),
 CONSTRAINT fk_employee_user FOREIGN KEY(user_id) REFERENCES users(id)
);
CREATE TABLE room_types (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 type_name VARCHAR(60) NOT NULL UNIQUE,
 description VARCHAR(255),
 price_per_night DECIMAL(12,2) NOT NULL,
 capacity INT NOT NULL DEFAULT 2,
 active BOOLEAN NOT NULL DEFAULT TRUE,
 CHECK(price_per_night >= 0), CHECK(capacity > 0)
);
CREATE TABLE rooms (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 room_number VARCHAR(20) NOT NULL UNIQUE,
 room_type_id BIGINT NOT NULL,
 floor_number INT NOT NULL DEFAULT 1,
 status ENUM('AVAILABLE','OCCUPIED','MAINTENANCE','INACTIVE') NOT NULL DEFAULT 'AVAILABLE',
 note VARCHAR(255),
 CONSTRAINT fk_room_type FOREIGN KEY(room_type_id) REFERENCES room_types(id)
);
CREATE TABLE bookings (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 booking_code VARCHAR(30) NOT NULL UNIQUE,
 customer_id BIGINT NOT NULL,
 created_by BIGINT,
 check_in DATE NOT NULL,
 check_out DATE NOT NULL,
 status ENUM('PENDING','CONFIRMED','CHECKED_IN','CHECKED_OUT','CANCELLED') NOT NULL DEFAULT 'CONFIRMED',
 note VARCHAR(500),
 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 CONSTRAINT fk_booking_customer FOREIGN KEY(customer_id) REFERENCES customers(id),
 CONSTRAINT fk_booking_user FOREIGN KEY(created_by) REFERENCES users(id),
 CHECK(check_out > check_in)
);
CREATE TABLE booking_details (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 booking_id BIGINT NOT NULL,
 room_id BIGINT NOT NULL,
 price_per_night DECIMAL(12,2) NOT NULL,
 nights INT NOT NULL,
 line_total DECIMAL(12,2) NOT NULL,
 CONSTRAINT fk_detail_booking FOREIGN KEY(booking_id) REFERENCES bookings(id),
 CONSTRAINT fk_detail_room FOREIGN KEY(room_id) REFERENCES rooms(id),
 CHECK(nights > 0), CHECK(line_total >= 0),
 UNIQUE KEY uk_booking_room(booking_id, room_id)
);
CREATE TABLE payments (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 booking_id BIGINT NOT NULL,
 amount DECIMAL(12,2) NOT NULL,
 method ENUM('CASH','CARD','BANK_TRANSFER') NOT NULL,
 status ENUM('PENDING','PAID','REFUNDED') NOT NULL DEFAULT 'PAID',
 paid_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 CONSTRAINT fk_payment_booking FOREIGN KEY(booking_id) REFERENCES bookings(id),
 CHECK(amount > 0)
);
CREATE TABLE invoices (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 invoice_code VARCHAR(30) NOT NULL UNIQUE,
 booking_id BIGINT NOT NULL UNIQUE,
 total_amount DECIMAL(12,2) NOT NULL,
 issued_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 CONSTRAINT fk_invoice_booking FOREIGN KEY(booking_id) REFERENCES bookings(id)
);

INSERT INTO roles(role_name) VALUES ('ADMIN'),('STAFF');
-- Mật khẩu demo mặc định cho cả admin và staff: password (BCrypt hash)
INSERT INTO users(username,password_hash,full_name,role_id) VALUES
('admin','$2a$12$Vs8evvdTnhQS2sB3hgTLu.J1HCop3cIbFbU4aIvYP8cgIANGdzrxi','Quản trị viên',1),
('staff','$2a$12$t289.G2aQ530R2rA01IveuHIQbzhgldl8jecp7MlgXhs.5mkcBdaq','Nhân viên lễ tân',2);
INSERT INTO employees(user_id,phone,position_name) VALUES (2,'0900000001','Lễ tân');

INSERT INTO room_types(type_name,description,price_per_night,capacity) VALUES
('Standard','Phòng tiêu chuẩn',500000,2),
('Deluxe','Phòng cao cấp',850000,2),
('Suite','Phòng suite',1500000,4);
INSERT INTO rooms(room_number,room_type_id,floor_number) VALUES
('101',1,1),('102',1,1),('103',2,1),('201',2,2),('202',3,2),('203',1,2);
INSERT INTO customers(full_name,phone,email,identity_number,address) VALUES
('Nguyễn Văn An','0912345678','an@example.com','079000000001','TP. Hồ Chí Minh'),
('Trần Thị Bình','0987654321','binh@example.com','079000000002','Đồng Nai');
