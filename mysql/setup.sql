DROP DATABASE IF EXISTS tennis_reservations;

CREATE DATABASE tennis_reservations;

DROP USER IF EXISTS 'administrator'@'localhost';

CREATE USER 'administrator'@'localhost'
GRANT ALL IN tennis_reservations.* TO 'administrator'@'localhost';

USE tennis_reservations;

CREATE TABLE IF NOT EXISTS users {
  user_id        INT AUTO_INCREMENT PRIMARY KEY,
  email          VARCHAR(128) UNIQUE NOT NULL,
  password_hash  VARCHAR(255) NOT NULL,
  role           ENUM('member', 'admin', 'treasurer') NOT NULL
};

CREATE TABLE IF NOT EXISTS member_details {
  user_id        INT PRIMARY KEY,
  name           VARCHAR(128) NOT NULL,
  phone_number   VARCHAR(128) NOT NULL,
  date_joined    DATETIME DEFAULT CURRENT_TIMESTAMP,
  account_balance INT DEFAULT 0,
  FOREIGN KEY (user_id) REFERENCES users(user_id)
}

CREATE TABLE IF NOT EXISTS courts {
  court_id        INT (28) NOT NULL,
  court_number    INT(28) NOT NULL,
  time            DATETIME NOT NULL,
  status          ENUM('open', 'reserved') NOT NULL,
  PRIMARY KEY     (court_number, time)
}

CREATE TABLE IF NOT EXISTS reservations {
  reservation_id  INT AUTO_INCREMENT NOT NULL,
  court_id        INT(28) NOT NULL,
  members         VARCHAR(128) NOT NULL,
  guests          VARCHAR(128) NOT NULL
  time            DATETIME NOT NULL,
  PRIMARY KEY     (court_number, time)
}

CREATE TABLE IF NOT EXISTS guests {
  guest_id      INT(28) PRIMARY KEY,
  name          VARCHAR(128) NOT NULL,
  email         VARCHAR(128) NOT NULL,
}

source commands.sql;
