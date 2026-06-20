-- Migration Script: V8__create_google_signup_token_table.sql
-- Description: Create google_signup_token table

CREATE TABLE public.google_signup_token (
                                            id UUID NOT NULL,
                                            token_hash VARCHAR(64) NOT NULL,
                                            email VARCHAR(255) NOT NULL,
                                            first_name VARCHAR(255),
                                            last_name VARCHAR(255),
                                            expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                            used_at TIMESTAMP WITH TIME ZONE,
                                            created_at TIMESTAMP WITH TIME ZONE NOT NULL,

    -- Constraints
                                            CONSTRAINT pk_google_signup_token PRIMARY KEY (id),
                                            CONSTRAINT uq_google_signup_token_token_hash UNIQUE (token_hash)
);
