CREATE TABLE IF NOT EXISTS inbox_threads (
    id BIGSERIAL PRIMARY KEY,
    guest VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    stay VARCHAR(255) NOT NULL,
    room VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    last_reply VARCHAR(255),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE IF NOT EXISTS inbox_messages (
    id BIGSERIAL PRIMARY KEY,
    thread_id BIGINT NOT NULL,
    sender VARCHAR(255) NOT NULL,
    body TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_inbox_messages_thread
        FOREIGN KEY (thread_id)
        REFERENCES inbox_threads (id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_inbox_threads_updated_at ON inbox_threads (updated_at DESC, id DESC);
CREATE INDEX IF NOT EXISTS idx_inbox_messages_thread_id ON inbox_messages (thread_id);

CREATE TABLE IF NOT EXISTS reservations (
    id BIGSERIAL PRIMARY KEY,
    guest VARCHAR(255) NOT NULL,
    property VARCHAR(255) NOT NULL,
    arrival VARCHAR(255) NOT NULL,
    payout VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_reservations_updated_at ON reservations (updated_at ASC, id ASC);

CREATE TABLE IF NOT EXISTS listings (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price VARCHAR(255) NOT NULL,
    location VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_listings_updated_at ON listings (updated_at DESC, id DESC);

CREATE TABLE IF NOT EXISTS app_users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_app_users_email ON app_users (email);
