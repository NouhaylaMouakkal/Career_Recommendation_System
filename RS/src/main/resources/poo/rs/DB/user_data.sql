CREATE TABLE user_data (
   id INT PRIMARY KEY AUTO_INCREMENT,
   user_id INT,
   level VARCHAR(255),
   hard_skills TEXT,
   soft_skills TEXT,
   FOREIGN KEY (user_id) REFERENCES users(ID)
);
