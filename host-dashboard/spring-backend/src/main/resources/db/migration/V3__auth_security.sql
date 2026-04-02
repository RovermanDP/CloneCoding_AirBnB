-- 계정 잠금: 로그인 실패 횟수 및 잠금 해제 시각
ALTER TABLE app_users ADD COLUMN login_attempts INT NOT NULL DEFAULT 0;
ALTER TABLE app_users ADD COLUMN locked_until TIMESTAMP WITH TIME ZONE;

-- Refresh Token 저장소
CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_refresh_tokens_token_hash ON refresh_tokens (token_hash);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);
