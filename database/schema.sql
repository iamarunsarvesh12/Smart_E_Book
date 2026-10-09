-- =========================================================
-- Smart E-Book Management System Database Script
-- Target Database: MySQL 8.0
-- Database Name: smart_ebook_db
-- =========================================================

CREATE DATABASE IF NOT EXISTS smart_ebook_db;
USE smart_ebook_db;

-- ---------------------------------------------------------
-- 1. Table: users
-- ---------------------------------------------------------
DROP TABLE IF EXISTS bookmarks;
DROP TABLE IF EXISTS downloads;
DROP TABLE IF EXISTS purchases;
DROP TABLE IF EXISTS books;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'user' CHECK (role IN ('user', 'admin')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------
-- 2. Table: books
-- ---------------------------------------------------------
CREATE TABLE books (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    category VARCHAR(100) NOT NULL,
    mood VARCHAR(50) NOT NULL,
    purpose VARCHAR(50) NOT NULL,
    summary TEXT NOT NULL,
    price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    pdf_path VARCHAR(255) NOT NULL,
    cover_path VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------
-- 3. Table: purchases
-- ---------------------------------------------------------
CREATE TABLE purchases (
    order_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    book_id INT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    payment_status VARCHAR(50) NOT NULL DEFAULT 'COMPLETED',
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_purchases_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_purchases_book FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------
-- 4. Table: downloads
-- ---------------------------------------------------------
CREATE TABLE downloads (
    download_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    book_id INT NOT NULL,
    download_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_downloads_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_downloads_book FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------
-- 5. Table: bookmarks
-- ---------------------------------------------------------
CREATE TABLE bookmarks (
    bookmark_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    book_id INT NOT NULL,
    last_page_read INT DEFAULT 1,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_bookmarks_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_bookmarks_book FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
    CONSTRAINT uq_user_book_bookmark UNIQUE (user_id, book_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================================================
-- Initial Seed Data
-- =========================================================

-- Note: Passwords stored as SHA-256 hash or secure default strings
-- 'admin123' -> 240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9
-- 'reader123' -> 128a1cb71e153e042708de7ea043d9a030fc1a83fa258788e7ef7aa23309eb72
INSERT INTO users (name, email, password, role) VALUES
('System Administrator', 'admin@ebook.com', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'admin'),
('Jane Reader', 'reader@ebook.com', '128a1cb71e153e042708de7ea043d9a030fc1a83fa258788e7ef7aa23309eb72', 'user');

-- 4 Sample Books covering different moods & purposes
INSERT INTO books (title, author, category, mood, purpose, summary, price, pdf_path, cover_path) VALUES
(
    'Atomic Focus & Productivity',
    'Dr. Evelyn Vance',
    'Self-Improvement',
    'Focus',
    'Career',
    'A tactical and scientific blueprint designed to eliminate mental fatigue, resist modern distractions, and master deep cognitive workflow in professional environments.',
    19.99,
    'uploads/books/atomic_focus.pdf',
    'uploads/covers/atomic_focus.jpg'
),
(
    'Serenity in Code: Mindful Software Engineering',
    'Marcus Aurelius Lin',
    'Technology & Philosophy',
    'Relaxed',
    'Leisure',
    'Explore calming perspectives and stoic philosophies applied to complex software design, architecture, and team collaboration without developer burnout.',
    14.50,
    'uploads/books/serenity_in_code.pdf',
    'uploads/covers/serenity_in_code.jpg'
),
(
    'Algorithmic Mastery & System Design Essentials',
    'Prof. Sarah Jenkins',
    'Computer Science',
    'Inspired',
    'Exam Prep',
    'Comprehensive reference guide containing rigorous algorithmic breakdowns, distributed systems architecture patterns, and step-by-step technical interview preparation solutions.',
    29.99,
    'uploads/books/algorithmic_mastery.pdf',
    'uploads/covers/algorithmic_mastery.jpg'
),
(
    'The Resilient Innovator',
    'David Sterling',
    'Business & Leadership',
    'Uplifting',
    'Study',
    'An inspiring narrative analyzing high-impact leaders who converted massive business crises into breakthrough technological revolutions and market opportunities.',
    22.00,
    'uploads/books/resilient_innovator.pdf',
    'uploads/covers/resilient_innovator.jpg'
);
