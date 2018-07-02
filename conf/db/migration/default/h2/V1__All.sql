CREATE TABLE roles (
  account_id                BIGINT UNSIGNED NOT NULL,
  role_name                 VARCHAR(100) NOT NULL,
  PRIMARY KEY (account_id, role_name)
);

CREATE TABLE sessions (
  id                        SERIAL PRIMARY KEY,
  user_id                   BIGINT UNSIGNED NOT NULL,
  ip                        VARCHAR(100) NOT NULL, 
  session_key               VARCHAR(100) NOT NULL UNIQUE,
  created                   BIGINT UNSIGNED NOT NULL,
  expire                    BIGINT UNSIGNED NOT NULL
);

CREATE TABLE accounts (
  id                        SERIAL PRIMARY KEY,
  login                     VARCHAR(100) NOT NULL UNIQUE,
  email                     VARCHAR(100) NOT NULL UNIQUE,
  hash                      VARCHAR(60),
  confirmation_status       VARCHAR(100) NOT NULL,
  account_status            VARCHAR(100) NOT NULL,
  registered                BIGINT UNSIGNED NOT NULL,
  confirm_code              VARCHAR(100)
);

CREATE TABLE short_options (
  id                        SERIAL PRIMARY KEY,
  name                      VARCHAR(100) NOT NULL,
  descr                     VARCHAR(255) NOT NULL,
  `type`                    VARCHAR(100) NOT NULL,
  `value`                   VARCHAR(100) NOT NULL
);

CREATE TABLE telegram_accounts (
  account_id                BIGINT UNSIGNED NOT NULL PRIMARY KEY,
  telegram_login            VARCHAR(100)
);

INSERT INTO accounts VALUES (1,'testadmin','testadmin@project.country','$2a$10$EwrXfFADQmgbfyY54fPMbuWCnmTSbCpl9Rfrkc0.3OrVp/GeBMTp6','confirmed','normal',1529936034487,NULL);

INSERT INTO short_options VALUES (1,'REGISTER_ALLOWED','Registration allowance','Boolean','true');

INSERT INTO roles VALUES (1,'client');
INSERT INTO roles VALUES (1,'admin');


