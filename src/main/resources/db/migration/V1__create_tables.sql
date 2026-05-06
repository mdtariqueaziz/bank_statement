CREATE TABLE IF NOT EXISTS users (
    id            BIGSERIAL     PRIMARY KEY,
    username      VARCHAR(100)  NOT NULL UNIQUE,
    password      VARCHAR(255)  NOT NULL,
    email         VARCHAR(150)  NOT NULL UNIQUE,
    role          VARCHAR(50)   NOT NULL DEFAULT 'USER',
    access_token  VARCHAR(2048),
    enabled       BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE  users IS 'API authenticated users (bank staff or system accounts)';
COMMENT ON COLUMN users.access_token IS 'Current active JWT — NULL means logged out';

CREATE TABLE IF NOT EXISTS statements (
    id                UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id        VARCHAR(30)   NOT NULL,
    transaction_date  DATE          NOT NULL,
    amount            NUMERIC(18,2) NOT NULL CHECK (amount > 0),
    type              VARCHAR(20)   NOT NULL CHECK (type IN ('CREDIT','DEBIT')),
    description       VARCHAR(255),
    balance           NUMERIC(18,2),
    reference_number  VARCHAR(50)   UNIQUE,
    currency          CHAR(3)       NOT NULL DEFAULT 'INR',
    created_at        TIMESTAMPTZ   NOT NULL DEFAULT NOW()
) PARTITION BY RANGE (transaction_date);

COMMENT ON TABLE  statements IS 'Bank transactions partitioned by date for scalability';
COMMENT ON COLUMN statements.account_id IS 'Bank account number — max 30 chars for Indian formats';

CREATE TABLE IF NOT EXISTS statements_2023
    PARTITION OF statements
    FOR VALUES FROM ('2023-01-01') TO ('2024-01-01');

CREATE TABLE IF NOT EXISTS statements_2024
    PARTITION OF statements
    FOR VALUES FROM ('2024-01-01') TO ('2025-01-01');

CREATE TABLE IF NOT EXISTS statements_2025
    PARTITION OF statements
    FOR VALUES FROM ('2025-01-01') TO ('2026-01-01');

CREATE TABLE IF NOT EXISTS statements_2026
    PARTITION OF statements
    FOR VALUES FROM ('2026-01-01') TO ('2027-01-01');

CREATE TABLE IF NOT EXISTS statements_default
    PARTITION OF statements DEFAULT;
