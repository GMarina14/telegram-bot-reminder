-- liquibase formatted sql

-- changeset GMarina14:1
CREATE TABLE IF NOT EXISTS notification_task(
    notification_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY ,
    chat_id BIGINT NOT NULL,
    notification TEXT NOT NULL,
    send_time TIMESTAMP NOT NULL
);