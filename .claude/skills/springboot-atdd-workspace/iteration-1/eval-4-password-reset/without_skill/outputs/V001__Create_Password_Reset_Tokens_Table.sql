-- Migration: Create password_reset_tokens table
-- This table stores password reset tokens for the password reset feature

CREATE TABLE password_reset_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(100) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign key to users table
    CONSTRAINT fk_password_reset_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Indexes for performance
CREATE INDEX idx_password_reset_tokens_token ON password_reset_tokens(token);
CREATE INDEX idx_password_reset_tokens_user_id ON password_reset_tokens(user_id);
CREATE INDEX idx_password_reset_tokens_expires_at ON password_reset_tokens(expires_at);

-- Comment on table
COMMENT ON TABLE password_reset_tokens IS 'Stores password reset tokens that are valid for 1 hour';
COMMENT ON COLUMN password_reset_tokens.id IS 'Primary key';
COMMENT ON COLUMN password_reset_tokens.user_id IS 'Reference to the user who requested the reset';
COMMENT ON COLUMN password_reset_tokens.token IS 'Unique reset token (UUID format)';
COMMENT ON COLUMN password_reset_tokens.expires_at IS 'Token expiration timestamp (1 hour from creation)';
COMMENT ON COLUMN password_reset_tokens.used IS 'Whether the token has been used';
COMMENT ON COLUMN password_reset_tokens.created_at IS 'Token creation timestamp';
