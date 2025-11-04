CREATE OR REPLACE FUNCTION fn_set_timestamps() RETURNS TRIGGER AS
$$
BEGIN
    IF TG_OP = 'INSERT' THEN
        NEW.created_at := NOW();
        NEW.updated_at := NOW();
    ELSIF TG_OP = 'UPDATE' THEN
        NEW.updated_at := NOW();
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

----------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------

CREATE TYPE USER_AUTH_PROVIDER AS ENUM ('GITHUB', 'GITLAB', 'GOOGLE', 'MICROSOFT');

CREATE TYPE ADMIN_ROLE AS ENUM ('ADMIN', 'SUPER_ADMIN');

----------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------

CREATE TABLE users (
    id            BIGINT GENERATED ALWAYS AS IDENTITY,
    uuid          UUID               NOT NULL,
    auth_provider USER_AUTH_PROVIDER NOT NULL,
    openid_sub    VARCHAR(255)       NOT NULL,
    profile_image VARCHAR,
    global_name   VARCHAR(255),
    global_email  VARCHAR(255),
    created_at    TIMESTAMP          NOT NULL,
    updated_at    TIMESTAMP          NOT NULL,
    deleted_at    TIMESTAMP
);

CREATE TABLE admins (
    id         BIGINT GENERATED ALWAYS AS IDENTITY,
    login_id   VARCHAR(255) NOT NULL,
    password   VARCHAR(255) NOT NULL,
    role       ADMIN_ROLE   NOT NULL,
    created_at TIMESTAMP    NOT NULL,
    updated_at TIMESTAMP    NOT NULL,
    deleted_at TIMESTAMP
);

----------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------

ALTER TABLE users
    ADD CONSTRAINT pk_users PRIMARY KEY (id);

ALTER TABLE admins
    ADD CONSTRAINT pk_admins PRIMARY KEY (id);

----------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------

CREATE UNIQUE INDEX idx_users_uuid_active ON users (uuid);

CREATE UNIQUE INDEX idx_users_auth_provider_openid_sub_active ON users (auth_provider, openid_sub) WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX idx_admins_login_id_active ON admins (login_id) WHERE deleted_at IS NULL;

----------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------

CREATE TRIGGER trg_users_timestamps
    BEFORE INSERT OR UPDATE
    ON users
    FOR EACH ROW
EXECUTE FUNCTION fn_set_timestamps();

CREATE TRIGGER trg_admins_timestamps
    BEFORE INSERT OR UPDATE
    ON admins
    FOR EACH ROW
EXECUTE FUNCTION fn_set_timestamps();