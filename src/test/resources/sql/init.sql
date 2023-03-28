CREATE TABLE IF NOT EXISTS users
(
    id         BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    first_name TEXT                  NOT NULL,
    last_name  TEXT                  NOT NULL,
    birthday   TIMESTAMP
);

CREATE TABLE notes
(
    id         BIGINT                                 NOT NULL PRIMARY KEY,
    body       VARCHAR(255)                           NOT NULL,
    created_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP NOT NULL,
    person_id  BIGINT
        CONSTRAINT person_FK references users (id)
);

CREATE SEQUENCE notes_seq;
