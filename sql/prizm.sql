CREATE TABLE admin_roles (
    code VARCHAR(255) NOT NULL PRIMARY KEY
);

INSERT INTO admin_roles (code)
VALUES ('ADMIN'),
       ('SUPER_ADMIN');

----------------------------------------------------------------------------------------------------

CREATE TABLE workspace_user_roles (
    code VARCHAR(255) NOT NULL PRIMARY KEY
);

INSERT INTO workspace_user_roles (code)
VALUES ('OWNER'),
       ('MANAGER'),
       ('MEMBER'),
       ('GUEST'),
       ('WEBHOOK'),
       ('ASSISTANT');

----------------------------------------------------------------------------------------------------

CREATE TABLE workspace_user_states (
    code VARCHAR(255) NOT NULL PRIMARY KEY
);

INSERT INTO workspace_user_states (code)
VALUES ('ONLINE'),
       ('AWAY'),
       ('BUSY'),
       ('OFFLINE');

----------------------------------------------------------------------------------------------------

CREATE TABLE workspace_user_notifies (
    code VARCHAR(255) NOT NULL PRIMARY KEY
);

INSERT INTO workspace_user_notifies(code)
VALUES ('ON'),
       ('MENTION'),
       ('OFF');

----------------------------------------------------------------------------------------------------

CREATE TABLE user_auth_providers (
    code VARCHAR(255) NOT NULL PRIMARY KEY
);

INSERT INTO user_auth_providers (code)
VALUES ('GOOGLE'),
       ('GITHUB');

----------------------------------------------------------------------------------------------------

CREATE TABLE languages (
    code VARCHAR(255) NOT NULL PRIMARY KEY
);

INSERT INTO languages (code)
VALUES ('KO'),
       ('EN'),
       ('JA'),
       ('FR');

----------------------------------------------------------------------------------------------------

CREATE TABLE channel_types (
    code VARCHAR(255) NOT NULL PRIMARY KEY
);

INSERT INTO channel_types (code)
VALUES ('CHAT'),
       ('DM'),
       ('WEBHOOK'),
       ('ASSISTANT');

----------------------------------------------------------------------------------------------------

CREATE TABLE channel_workspace_user_notifies (
    code VARCHAR(255) NOT NULL PRIMARY KEY
);

INSERT INTO channel_workspace_user_notifies(code)
VALUES ('ON'),
       ('MENTION'),
       ('OFF');

----------------------------------------------------------------------------------------------------

CREATE TABLE group_channel_permissions (
    code VARCHAR(255) NOT NULL PRIMARY KEY
);

INSERT INTO group_channel_permissions (code)
VALUES ('READ'),
       ('WRITE'),
       ('MANAGE');

----------------------------------------------------------------------------------------------------

CREATE TABLE message_types (
    code VARCHAR(255) NOT NULL PRIMARY KEY
);

INSERT INTO message_types (code)
VALUES ('STRING'),
       ('LINK'),
       ('MEDIA'),
       ('DOCUMENT'),
       ('ETC');

----------------------------------------------------------------------------------------------------

CREATE TABLE ai_rag_progresses (
    code VARCHAR(255) NOT NULL PRIMARY KEY
);

INSERT INTO ai_rag_progresses(code)
VALUES ('QUEUE'),
       ('IN_PROGRESS'),
       ('SUCCESS'),
       ('FAILURE');

----------------------------------------------------------------------------------------------------

CREATE TABLE notify_types (
    code VARCHAR(255) NOT NULL PRIMARY KEY
);

INSERT INTO notify_types (code)
VALUES ('SYSTEM'),
       ('INVITE'),
       ('MESSAGE');

----------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------

CREATE TABLE admins (
    id         BIGINT       NOT NULL PRIMARY KEY,
    identity   VARCHAR(255) NOT NULL,
    password   VARCHAR(255) NOT NULL,
    role_code  VARCHAR(255) NOT NULL REFERENCES admin_roles (code),
    created_at TIMESTAMP    NOT NULL,
    updated_at TIMESTAMP    NOT NULL,
    deleted_at TIMESTAMP    NULL
);

CREATE UNIQUE INDEX uq_admins_identity_active ON admins (identity) WHERE deleted_at IS NULL;

----------------------------------------------------------------------------------------------------

CREATE TABLE users (
    id                 BIGINT       NOT NULL PRIMARY KEY,
    auth_provider_code VARCHAR(255) NOT NULL REFERENCES user_auth_providers (code),
    openid_sub         VARCHAR(255) NOT NULL,
    profile_image_path VARCHAR      NULL,
    name               VARCHAR(255) NOT NULL,
    email              VARCHAR(255) NOT NULL,
    language_code      VARCHAR(255) NOT NULL REFERENCES languages (code),
    created_at         TIMESTAMP    NOT NULL,
    updated_at         TIMESTAMP    NOT NULL,
    deleted_at         TIMESTAMP    NULL
);

CREATE UNIQUE INDEX uq_users_auth_provider_code_openid_sub_active ON users (auth_provider_code, openid_sub) WHERE deleted_at IS NULL;

----------------------------------------------------------------------------------------------------

CREATE TABLE workspaces (
    id              BIGINT       NOT NULL PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    icon_image_path VARCHAR      NULL,
    ai_api_key      VARCHAR(255) NULL,
    created_at      TIMESTAMP    NOT NULL,
    updated_at      TIMESTAMP    NOT NULL,
    deleted_at      TIMESTAMP    NULL
);

----------------------------------------------------------------------------------------------------

CREATE TABLE workspace_users (
    id                 BIGINT          NOT NULL PRIMARY KEY,
    workspace_id       BIGINT          NOT NULL REFERENCES workspaces (id),
    user_id            BIGINT          NOT NULL REFERENCES users (id),
    role_code          VARCHAR(255)    NOT NULL REFERENCES workspace_user_roles (code),
    profile_image_path VARCHAR         NULL,
    name               VARCHAR(255)    NULL,
    email              VARCHAR(255)    NULL,
    phone              VARCHAR(255)    NULL,
    state_code         VARCHAR(255)    NOT NULL REFERENCES workspace_user_states (code),
    notify_code        VARCHAR(255)    NOT NULL REFERENCES workspace_user_notifies (code),
    z_index            DECIMAL(20, 10) NOT NULL,
    created_at         TIMESTAMP       NOT NULL,
    updated_at         TIMESTAMP       NOT NULL,
    deleted_at         TIMESTAMP       NULL
);

CREATE UNIQUE INDEX uq_workspace_users_workspace_id_user_id_active ON workspace_users (workspace_id, user_id) WHERE deleted_at IS NULL;

----------------------------------------------------------------------------------------------------

CREATE TABLE ai_rags (
    id                BIGINT       NOT NULL PRIMARY KEY,
    workspace_id      BIGINT       NOT NULL REFERENCES workspaces (id),
    workspace_user_id BIGINT       NOT NULL REFERENCES workspace_users (id),
    progress_code     VARCHAR(255) NOT NULL REFERENCES ai_rag_progresses (code),
    name              VARCHAR      NOT NULL,
    extension         VARCHAR(255) NOT NULL,
    size              BIGINT       NOT NULL,
    path              VARCHAR      NOT NULL,
    created_at        TIMESTAMP    NOT NULL
);

----------------------------------------------------------------------------------------------------

CREATE TABLE groups (
    id           BIGINT          NOT NULL PRIMARY KEY,
    workspace_id BIGINT          NOT NULL REFERENCES workspaces (id),
    name         VARCHAR(255)    NOT NULL,
    z_index      DECIMAL(20, 10) NOT NULL,
    created_at   TIMESTAMP       NOT NULL,
    updated_at   TIMESTAMP       NOT NULL,
    deleted_at   TIMESTAMP       NULL
);

----------------------------------------------------------------------------------------------------

CREATE TABLE categories (
    id           BIGINT          NOT NULL PRIMARY KEY,
    workspace_id BIGINT          NOT NULL REFERENCES workspaces (id),
    name         VARCHAR(255)    NOT NULL,
    z_index      DECIMAL(20, 10) NOT NULL,
    created_at   TIMESTAMP       NOT NULL,
    updated_at   TIMESTAMP       NOT NULL,
    deleted_at   TIMESTAMP       NULL
);

----------------------------------------------------------------------------------------------------

CREATE TABLE channels (
    id             BIGINT          NOT NULL PRIMARY KEY,
    workspace_id   BIGINT          NOT NULL REFERENCES workspaces (id),
    category_id    BIGINT          NULL REFERENCES categories (id),
    type_code      VARCHAR(255)    NOT NULL REFERENCES channel_types (code),
    name           VARCHAR(255)    NOT NULL,
    description    VARCHAR(255)    NULL,
    webhook_secret VARCHAR(255)    NULL,
    z_index        DECIMAL(20, 10) NOT NULL,
    created_at     TIMESTAMP       NOT NULL,
    updated_at     TIMESTAMP       NOT NULL,
    deleted_at     TIMESTAMP       NULL
);

----------------------------------------------------------------------------------------------------

CREATE TABLE group_workspace_users (
    id                BIGINT    NOT NULL PRIMARY KEY,
    group_id          BIGINT    NOT NULL REFERENCES groups (id),
    workspace_user_id BIGINT    NOT NULL REFERENCES workspace_users (id),
    created_at        TIMESTAMP NOT NULL
);

CREATE UNIQUE INDEX uq_group_workspace_users_group_id_workspace_user_id ON group_workspace_users (group_id, workspace_user_id);

----------------------------------------------------------------------------------------------------

CREATE TABLE group_channels (
    id              BIGINT       NOT NULL PRIMARY KEY,
    group_id        BIGINT       NOT NULL REFERENCES groups (id),
    channel_id      BIGINT       NOT NULL REFERENCES channels (id),
    permission_code VARCHAR(255) NOT NULL REFERENCES group_channel_permissions (code),
    created_at      TIMESTAMP    NOT NULL
);

CREATE UNIQUE INDEX uq_group_channels_group_id_channel_id ON group_channels (group_id, channel_id);

----------------------------------------------------------------------------------------------------

CREATE TABLE channel_workspace_users (
    id                BIGINT       NOT NULL PRIMARY KEY,
    channel_id        BIGINT       NOT NULL REFERENCES channels (id),
    workspace_user_id BIGINT       NOT NULL REFERENCES workspace_users (id),
    notify_code       VARCHAR(255) NOT NULL REFERENCES channel_workspace_user_notifies (code),
    created_at        TIMESTAMP    NOT NULL
);

CREATE UNIQUE INDEX uq_channel_workspace_users_channel_id_workspace_user_id ON channel_workspace_users (channel_id, workspace_user_id);

----------------------------------------------------------------------------------------------------

CREATE TABLE messages (
    id                BIGINT       NOT NULL PRIMARY KEY,
    channel_id        BIGINT       NOT NULL REFERENCES channels (id),
    workspace_user_id BIGINT       NOT NULL REFERENCES workspace_users (id),
    type_code         VARCHAR(255) NOT NULL REFERENCES message_types (code),
    content           VARCHAR      NULL,
    is_pinned         BOOLEAN      NOT NULL,
    reply_id          BIGINT       NULL REFERENCES messages (id),
    thread_id         BIGINT       NULL REFERENCES messages (id),
    created_at        TIMESTAMP    NOT NULL,
    updated_at        TIMESTAMP    NOT NULL,
    deleted_at        TIMESTAMP    NULL
);

----------------------------------------------------------------------------------------------------

CREATE TABLE message_files (
    id         BIGINT  NOT NULL PRIMARY KEY,
    message_id BIGINT  NOT NULL REFERENCES messages (id),
    name       VARCHAR NOT NULL,
    path       VARCHAR NOT NULL,
    size       BIGINT  NOT NULL
);

----------------------------------------------------------------------------------------------------

CREATE TABLE message_emojis (
    id                BIGINT       NOT NULL PRIMARY KEY,
    message_id        BIGINT       NOT NULL REFERENCES messages (id),
    workspace_user_id BIGINT       NOT NULL REFERENCES workspace_users (id),
    emoji             VARCHAR(255) NOT NULL,
    created_at        TIMESTAMP    NOT NULL
);

CREATE UNIQUE INDEX uq_message_emojis_message_id_workspace_user_id_emoji ON message_emojis (message_id, workspace_user_id, emoji);

----------------------------------------------------------------------------------------------------

CREATE TABLE message_translations (
    id            BIGINT       NOT NULL PRIMARY KEY,
    message_id    BIGINT       NOT NULL REFERENCES messages (id),
    language_code VARCHAR(255) NOT NULL REFERENCES languages (code),
    content       VARCHAR      NOT NULL,
    created_at    TIMESTAMP    NOT NULL
);

CREATE UNIQUE INDEX uq_message_translations_message_id_language_code ON message_translations (message_id, language_code);

----------------------------------------------------------------------------------------------------

CREATE TABLE notify (
    id                BIGINT       NOT NULL PRIMARY KEY,
    user_id           BIGINT       NOT NULL REFERENCES users (id),
    is_read           BOOLEAN      NOT NULL,
    is_important      BOOLEAN      NOT NULL,
    type_code         VARCHAR(255) NOT NULL REFERENCES notify_types (code),
    content           VARCHAR      NOT NULL,
    workspace_user_id BIGINT       NULL REFERENCES workspace_users (id),
    workspace_id      BIGINT       NULL REFERENCES workspaces (id),
    channel_id        BIGINT       NULL REFERENCES channels (id),
    message_id        BIGINT       NULL REFERENCES messages (id),
    created_at        TIMESTAMP    NOT NULL
);