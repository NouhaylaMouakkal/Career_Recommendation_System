CREATE DATABASE login;
USE login;
CREATE TABLE users(
  ID INT PRIMARY KEY AUTO_INCREMENT,
  USERNAME VARCHAR(20) NOT NULL,
  PASSWORD VARCHAR(20) NOT NULL
);
INSERT INTO users (USERNAME, PASSWORD) VALUES ('admin', 'admin');
