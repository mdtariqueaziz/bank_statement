CREATE INDEX IF NOT EXISTS idx_stmt_account_date
    ON statements (account_id, transaction_date DESC)
    INCLUDE (type, amount, id);

CREATE INDEX IF NOT EXISTS idx_stmt_account_date_id
    ON statements (account_id, transaction_date DESC, id DESC);

CREATE UNIQUE INDEX IF NOT EXISTS idx_stmt_reference
    ON statements (reference_number)
    WHERE reference_number IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_users_access_token
    ON users (access_token)
    WHERE access_token IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_users_username
    ON users (username);

CREATE INDEX IF NOT EXISTS idx_users_email
    ON users (email);

ANALYZE statements;
ANALYZE users;
