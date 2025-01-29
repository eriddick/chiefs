DROP DATABASE IF EXISTS tennis_reservations;

CREATE DATABASE tennis_scheduler;

DROP USER IF EXISTS 'administrator'@'localhost';

CREATE USER 'administrator'@'localhost'
GRANT ALL IN tennis_reservations.* TO 'administrator'@'localhost';

USE tennis_reservations;

CREATE TABLE IF NOT EXISTS members {
  name          VARCHAR(128) NOT NULL,
  phone_number  VARCHAR(128),
  email         VARCHAR(128),
  date_joined   DATETIME NOT NULL,
  PRIMARY KEY   (name, email)
};

CREATE TABLE IF NOT EXISTS admin_members {
  user_id         VARCHAR(128) NOT NULL,
  name            VARCHAR(128) NOT NULL,
  phone_number    VARCHAR(128) NOT NULL,
  email           VARCHAR(128) NOT NULL,
  date_joined     DATETIME NOT NULL,
  PRIMARY KEY     (user_id, name)
};

CREATE TABLE IF NOT EXISTS treasurer_members {
  user_id         VARCHAR(128) NOT NULL,
  name            VARCHAR(128) NOT NULL,
  phone_number    VARCHAR(128) NOT NULL,
  email           VARCHAR(128) NOT NULL,
  account balance INT(28) NOT NULL,
  PRIMARY KEY     (user_id, name)
};



CREATE TABLE IF NOT EXISTS all_members {
  user_id         VARCHAR(128) NOT NULL,
  name            VARCHAR(128) NOT NULL,
  phone_number    VARCHAR(128) NOT NULL,
  email           VARCHAR(128) NOT NULL,
  date_joined     DATETIME NOT NULL,
  account_balance INT(28) NOT NULL,
  PRIMARY KEY     (user_id, name)
}

source commands.sql;
