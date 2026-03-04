-- V1: Create instrument_tokens table
-- Stores the mapping between stock symbols and Kite Connect instrument tokens

CREATE TABLE instrument_tokens (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    stock_symbol      VARCHAR(20)  NOT NULL UNIQUE,
    instrument_token  BIGINT       NOT NULL UNIQUE,
    exchange          VARCHAR(10),
    trading_symbol    VARCHAR(50),
    active            BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP

);

-- Indexes for common lookups
CREATE INDEX idx_instrument_tokens_symbol ON instrument_tokens (stock_symbol);
CREATE INDEX idx_instrument_tokens_token ON instrument_tokens (instrument_token);
CREATE INDEX idx_instrument_tokens_active ON instrument_tokens (active);

-- Seed some common NSE instruments (instrument tokens are illustrative)
INSERT INTO instrument_tokens (stock_symbol, instrument_token, exchange, trading_symbol) VALUES
    ('RELIANCE', 738561, 'NSE', 'NSE:RELIANCE'),
    ('TCS', 2953217, 'NSE', 'NSE:TCS'),
    ('INFY', 408065, 'NSE', 'NSE:INFY'),
    ('HDFCBANK', 341249, 'NSE', 'NSE:HDFCBANK'),
    ('ICICIBANK', 1270529, 'NSE', 'NSE:ICICIBANK'),
    ('SBIN', 779521, 'NSE', 'NSE:SBIN'),
    ('BHARTIARTL', 2714625, 'NSE', 'NSE:BHARTIARTL'),
    ('ITC', 424961, 'NSE', 'NSE:ITC'),
    ('KOTAKBANK', 492033, 'NSE', 'NSE:KOTAKBANK'),
    ('LT', 2939649, 'NSE', 'NSE:LT');

COMMENT ON TABLE instrument_tokens IS 'Maps stock symbols to Kite Connect instrument tokens for WebSocket subscriptions';
