-- liquibase formatted sql

-- changeset GMarina14:1
CREATE TABLE IF NOT EXISTS notification_task
(
    notification_id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    chat_id BIGINT NOT NULL,
    notification_text TEXT NOT NULL,
    notification_send_time TIMESTAMP NOT NULL
);