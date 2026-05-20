CREATE TABLE IF NOT EXISTS messages (
  id INT AUTO_INCREMENT PRIMARY KEY,
  content VARCHAR(255) NOT NULL
);

INSERT INTO messages (content) VALUES
  ('Hello, world!'),
  ('Another message');
