CREATE TABLE files (
    id         BIGINT      NOT NULL PRIMARY KEY,
    name       TEXT        NOT NULL,
    extension  TEXT        NULL,
    size       BIGINT      NOT NULL,
    path       TEXT        NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

----------------------------------------------------------------------------------------------------

CREATE TABLE users (
    id            BIGINT      NOT NULL PRIMARY KEY,
    auth_provider TEXT        NOT NULL,
    openid_sub    TEXT        NOT NULL,
    image_id      BIGINT      NULL REFERENCES files (id),
    name          TEXT        NOT NULL,
    email         TEXT        NOT NULL,
    language      TEXT        NOT NULL,
    active        BOOLEAN     NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL,
    updated_at    TIMESTAMPTZ NOT NULL,
    deleted_at    TIMESTAMPTZ NULL
);

CREATE UNIQUE INDEX uq_users_auth_provider_openid_sub_active ON users (auth_provider, openid_sub) WHERE deleted_at IS NULL;

----------------------------------------------------------------------------------------------------

CREATE TABLE user_notify (
    id          BIGINT      NOT NULL PRIMARY KEY,
    receiver_id BIGINT      NOT NULL REFERENCES users (id),
    type        TEXT        NOT NULL,
    sender_id   BIGINT      NULL,
    content     TEXT        NOT NULL,
    location_id BIGINT      NULL,
    important   BOOLEAN     NOT NULL,
    read        BOOLEAN     NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL
);

----------------------------------------------------------------------------------------------------

CREATE TABLE workspaces (
    id         BIGINT      NOT NULL PRIMARY KEY,
    name       TEXT        NOT NULL,
    image_id   BIGINT      NULL REFERENCES files (id),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    deleted_at TIMESTAMPTZ NULL
);

----------------------------------------------------------------------------------------------------

CREATE TABLE workspace_users (
    id           BIGINT      NOT NULL PRIMARY KEY,
    workspace_id BIGINT      NOT NULL REFERENCES workspaces (id),
    user_id      BIGINT      NOT NULL REFERENCES users (id),
    image_id     BIGINT      NULL REFERENCES files (id),
    name         TEXT        NULL,
    email        TEXT        NULL,
    phone        TEXT        NULL,
    introduction TEXT        NULL,
    role         TEXT        NOT NULL,
    state        TEXT        NOT NULL,
    notify       TEXT        NOT NULL,
    banned       BOOLEAN     NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL,
    updated_at   TIMESTAMPTZ NOT NULL,
    deleted_at   TIMESTAMPTZ NULL
);

CREATE UNIQUE INDEX uq_workspace_users_workspace_id_user_id_active ON workspace_users (workspace_id, user_id) WHERE deleted_at IS NULL;

----------------------------------------------------------------------------------------------------

CREATE TABLE ai_rags (
    id                BIGINT      NOT NULL PRIMARY KEY,
    workspace_id      BIGINT      NOT NULL REFERENCES workspaces (id),
    workspace_user_id BIGINT      NOT NULL,
    file_id           BIGINT      NOT NULL REFERENCES files (id),
    progress          TEXT        NOT NULL,
    created_at        TIMESTAMPTZ NOT NULL,
    updated_at        TIMESTAMPTZ NOT NULL,
    deleted_at        TIMESTAMPTZ NULL
);

----------------------------------------------------------------------------------------------------

CREATE TABLE groups (
    id           BIGINT      NOT NULL PRIMARY KEY,
    workspace_id BIGINT      NOT NULL REFERENCES workspaces (id),
    name         TEXT        NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL,
    updated_at   TIMESTAMPTZ NOT NULL,
    deleted_at   TIMESTAMPTZ NULL
);

----------------------------------------------------------------------------------------------------

CREATE TABLE group_workspace_users (
    id                BIGINT      NOT NULL PRIMARY KEY,
    group_id          BIGINT      NOT NULL REFERENCES groups (id),
    workspace_user_id BIGINT      NOT NULL REFERENCES workspace_users (id),
    created_at        TIMESTAMPTZ NOT NULL
);

CREATE UNIQUE INDEX uq_group_workspace_users_group_id_workspace_user_id ON group_workspace_users (group_id, workspace_user_id);

----------------------------------------------------------------------------------------------------

CREATE TABLE categories (
    id           BIGINT         NOT NULL PRIMARY KEY,
    workspace_id BIGINT         NOT NULL REFERENCES workspaces (id),
    name         TEXT           NOT NULL,
    z_index      NUMERIC(10, 5) NOT NULL,
    created_at   TIMESTAMPTZ    NOT NULL,
    updated_at   TIMESTAMPTZ    NOT NULL,
    deleted_at   TIMESTAMPTZ    NULL
);

----------------------------------------------------------------------------------------------------

CREATE TABLE channels (
    id           BIGINT         NOT NULL PRIMARY KEY,
    workspace_id BIGINT         NOT NULL REFERENCES workspaces (id),
    category_id  BIGINT         NULL REFERENCES categories (id),
    type         TEXT           NOT NULL,
    name         TEXT           NOT NULL,
    description  TEXT           NULL,
    z_index      NUMERIC(10, 5) NOT NULL,
    created_at   TIMESTAMPTZ    NOT NULL,
    updated_at   TIMESTAMPTZ    NOT NULL,
    deleted_at   TIMESTAMPTZ    NULL
);

----------------------------------------------------------------------------------------------------

CREATE TABLE channel_webhooks (
    id         BIGINT NOT NULL PRIMARY KEY,
    channel_id BIGINT NOT NULL REFERENCES channels (id),
    name       TEXT   NOT NULL,
    image_id   BIGINT NOT NULL REFERENCES files (id),
    secret     TEXT   NOT NULL
);

----------------------------------------------------------------------------------------------------

CREATE TABLE channel_workspace_users (
    id                BIGINT      NOT NULL PRIMARY KEY,
    channel_id        BIGINT      NOT NULL REFERENCES channels (id),
    workspace_user_id BIGINT      NOT NULL REFERENCES workspace_users (id),
    explicit          BOOLEAN     NOT NULL,
    notify            TEXT        NOT NULL,
    created_at        TIMESTAMPTZ NOT NULL
);

CREATE UNIQUE INDEX uq_channel_workspace_users_channel_id_workspace_user_id ON channel_workspace_users (channel_id, workspace_user_id);

----------------------------------------------------------------------------------------------------

CREATE TABLE group_channels (
    id         BIGINT      NOT NULL PRIMARY KEY,
    group_id   BIGINT      NOT NULL REFERENCES groups (id),
    channel_id BIGINT      NOT NULL REFERENCES channels (id),
    permission TEXT        NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE UNIQUE INDEX uq_group_channels_group_id_channel_id ON group_channels (group_id, channel_id);

----------------------------------------------------------------------------------------------------

CREATE TABLE messages (
    id                BIGINT      NOT NULL PRIMARY KEY,
    channel_id        BIGINT      NOT NULL REFERENCES channels (id),
    type              TEXT        NOT NULL,
    workspace_user_id BIGINT      NULL,
    content           TEXT        NULL,
    file_id           BIGINT      NULL REFERENCES files (id),
    edited            BOOLEAN     NOT NULL,
    pinned            BOOLEAN     NOT NULL,
    reply_id          BIGINT      NULL,
    thread_id         BIGINT      NULL,
    created_at        TIMESTAMPTZ NOT NULL,
    updated_at        TIMESTAMPTZ NOT NULL,
    deleted_at        TIMESTAMPTZ NULL
);

----------------------------------------------------------------------------------------------------

CREATE TABLE message_emojis (
    id                BIGINT      NOT NULL PRIMARY KEY,
    message_id        BIGINT      NOT NULL REFERENCES messages (id),
    workspace_user_id BIGINT      NOT NULL,
    emoji             TEXT        NOT NULL,
    created_at        TIMESTAMPTZ NOT NULL
);

CREATE UNIQUE INDEX uq_message_emojis_message_id_workspace_user_id_emoji ON message_emojis (message_id, workspace_user_id, emoji);

----------------------------------------------------------------------------------------------------

CREATE TABLE message_translations (
    id         BIGINT      NOT NULL PRIMARY KEY,
    message_id BIGINT      NOT NULL REFERENCES messages (id),
    language   TEXT        NOT NULL,
    content    TEXT        NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE UNIQUE INDEX uq_message_translations_message_id_language ON message_translations (message_id, language);