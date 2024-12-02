-- =================================================================================================================
-- jbst
-- =================================================================================================================
CREATE TABLE "jbst_users" (
    "id" varchar(36) PRIMARY KEY,
    "username" varchar(255) NOT NULL,
    "password" varchar(255) NOT NULL,
    "zone_id" varchar(255) NOT NULL,
    "authorities" varchar(1024) NOT NULL,
    "email" varchar(255),
    "name" varchar(255),
    "password_change_required" bool NOT NULL,
    "email_confirmation_required" bool NOT NULL,
    "attributes" varchar(65535)
);

CREATE TABLE "jbst_users_sessions" (
    "id" varchar(36) PRIMARY KEY,
    "created_at" int8 NOT NULL,
    "updated_at" int8 NOT NULL,
    "username" varchar(255) NOT NULL,
    "access_token" varchar(4096) NOT NULL,
    "refresh_token" varchar(4096) NOT NULL,
    "metadata" varchar(65535) NOT NULL,
    "metadata_renew_cron" bool NOT NULL,
    "metadata_renew_manually" bool NOT NULL
);

CREATE TABLE "jbst_invitations" (
    "id" varchar(36),
    "owner" varchar(255) NOT NULL,
    "authorities" varchar(1024) NOT NULL,
    "code" varchar(40) NOT NULL,
    "invited" varchar
);

-- =================================================================================================================
-- SERVER
-- =================================================================================================================
CREATE TABLE "anything" (
    "id" varchar(36) PRIMARY KEY,
    "username" varchar(255) NOT NULL,
    "value" varchar
);

