DROP DATABASE IF EXISTS chatapp;
CREATE DATABASE chatapp;
USE chatapp;

CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash CHAR(64) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE messages (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sender VARCHAR(50) NOT NULL,
    receiver VARCHAR(50) NOT NULL,
    message TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO users (username, password_hash) VALUES
    ('alice', SHA2('alice123', 256)),
    ('bob', SHA2('bob123', 256)),
    ('charlie', SHA2('charlie123', 256)),
    ('admin', SHA2('admin123', 256));
