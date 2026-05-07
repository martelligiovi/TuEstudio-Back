ALTER TABLE users ALTER COLUMN hashed_password DROP NOT NULL;
ALTER TABLE users ADD COLUMN IF NOT EXISTS provider VARCHAR(20) NOT NULL DEFAULT 'LOCAL';
ALTER TABLE users ADD COLUMN IF NOT EXISTS provider_user_id VARCHAR(255);
CREATE UNIQUE INDEX IF NOT EXISTS idx_users_provider_pid ON users(provider, provider_user_id)
    WHERE provider_user_id IS NOT NULL;
