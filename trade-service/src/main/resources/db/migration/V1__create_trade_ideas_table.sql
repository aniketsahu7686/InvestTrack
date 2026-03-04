-- V1: Create trade_ideas table
-- Trade ideas store submitted trade signals with entry, SL, target, and status tracking

CREATE TABLE trade_ideas (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id           UUID            NOT NULL,
    trader_username   VARCHAR(50)     NOT NULL,
    stock_symbol      VARCHAR(20)     NOT NULL,
    entry_price       NUMERIC(12, 2)  NOT NULL,
    stop_loss         NUMERIC(12, 2)  NOT NULL,
    target_price      NUMERIC(12, 2)  NOT NULL,
    risk_reward_ratio NUMERIC(5, 2)   NOT NULL,
    risk_percentage   NUMERIC(5, 2),
    timeframe         VARCHAR(20)     NOT NULL,
    reason            VARCHAR(20)     NOT NULL,
    status            VARCHAR(20)     NOT NULL DEFAULT 'OPEN',
    notes             TEXT,
    created_at        TIMESTAMP       NOT NULL DEFAULT NOW(),
    closed_at         TIMESTAMP,

    -- Ensure entry > stop_loss and target > entry for long trades
    CONSTRAINT chk_entry_above_sl CHECK (entry_price > stop_loss),
    CONSTRAINT chk_target_above_entry CHECK (target_price > entry_price),
    CONSTRAINT chk_risk_reward_positive CHECK (risk_reward_ratio > 0)
);

-- Indexes for common query patterns
CREATE INDEX idx_trade_ideas_user_id ON trade_ideas (user_id);
CREATE INDEX idx_trade_ideas_status ON trade_ideas (status);
CREATE INDEX idx_trade_ideas_stock_symbol ON trade_ideas (stock_symbol);
CREATE INDEX idx_trade_ideas_timeframe ON trade_ideas (timeframe);
CREATE INDEX idx_trade_ideas_created_at ON trade_ideas (created_at DESC);
CREATE INDEX idx_trade_ideas_symbol_status ON trade_ideas (stock_symbol, status);

COMMENT ON TABLE trade_ideas IS 'Stores submitted trade ideas with entry/exit levels and status tracking';
COMMENT ON COLUMN trade_ideas.risk_reward_ratio IS 'Auto-calculated: (target - entry) / (entry - stopLoss)';
COMMENT ON COLUMN trade_ideas.status IS 'OPEN, TARGET_HIT, SL_HIT, or EXPIRED';
