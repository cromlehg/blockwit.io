CREATE TABLE roles (
  account_id                BIGINT UNSIGNED NOT NULL,
  role_name                 ENUM('client', 'editor', 'admin') NOT NULL,
  PRIMARY KEY (account_id, role_name)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

CREATE TABLE sessions (
  id                        SERIAL PRIMARY KEY,
  user_id                   BIGINT UNSIGNED NOT NULL,
  ip                        VARCHAR(100) NOT NULL, 
  session_key               VARCHAR(100) NOT NULL UNIQUE,
  created                   BIGINT UNSIGNED NOT NULL,
  expire                    BIGINT UNSIGNED NOT NULL
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

CREATE TABLE accounts (
  id                        SERIAL PRIMARY KEY,
  login                     VARCHAR(100) NOT NULL UNIQUE,
  email                     VARCHAR(100) NOT NULL UNIQUE,
  hash                      VARCHAR(60),
  confirmation_status       ENUM('confirmed', 'wait confirmation') NOT NULL,
  account_status            ENUM('normal', 'locked') NOT NULL,
  registered                BIGINT UNSIGNED NOT NULL,
  confirm_code              VARCHAR(100)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

CREATE TABLE short_options (
  id                        SERIAL PRIMARY KEY,
  name                      VARCHAR(100) NOT NULL,
  descr                     VARCHAR(255) NOT NULL,
  `type`                    VARCHAR(100) NOT NULL,
  `value`                   VARCHAR(100) NOT NULL
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

CREATE TABLE telegram_accounts (
  account_id                BIGINT UNSIGNED NOT NULL PRIMARY KEY,
  telegram_login            VARCHAR(100)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

INSERT INTO short_options VALUES (1,'REGISTER_ALLOWED','Registration allowance','Boolean','true');



