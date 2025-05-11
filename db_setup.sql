-- Database setup script for Library Management System

-- Make sure we're using the right database
USE library_db;

-- Create the books table if it doesn't exist
CREATE TABLE IF NOT EXISTS books (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  author VARCHAR(255) NOT NULL
);

-- Insert some sample data
INSERT INTO books (name, author) VALUES
('To Kill a Mockingbird', 'Harper Lee'),
('1984', 'George Orwell'),
('Pride and Prejudice', 'Jane Austen'),
('The Great Gatsby', 'F. Scott Fitzgerald'),
('The Catcher in the Rye', 'J.D. Salinger');

-- Show the inserted data
SELECT * FROM books;