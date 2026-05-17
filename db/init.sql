-- SQL initialization script
--
-- This script runs automatically when the MySQL container starts for the
-- first time.  It creates a simple table and prepopulates it with
-- example data.  You can extend this file with additional schema
-- definitions or seed data as your application evolves.

CREATE TABLE IF NOT EXISTS messages (
  id INT AUTO_INCREMENT PRIMARY KEY,
  content VARCHAR(255) NOT NULL
);

INSERT INTO messages (content) VALUES
  ('Hello, world!'),
  ('Another message');