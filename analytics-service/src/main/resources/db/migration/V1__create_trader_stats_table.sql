-- V1: Create trader_stats table
-- Stores aggregated performance statistics for each trader

CREATE TABLE trader_stats (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id                 UUID            NOT NULL UNIQUE,
    username                VARCHAR(50),
    total_trades            INTEGER         NOT NULL DEFAULT 0,
    wins                    INTEGER         NOT NULL DEFAULT 0,
    losses                  INTEGER         NOT NULL DEFAULT 0,
    expired                 INTEGER         NOT NULL DEFAULT 0,
    win_rate                NUMERIC(5, 4)   NOT NULL DEFAULT 0,
    avg_risk_reward         NUMERIC(5, 2)   NOT NULL DEFAULT 0,
    total_risk_reward_sum   NUMERIC(10, 2)  NOT NULL DEFAULT 0,
    consistency_score       NUMERIC(5, 2)   NOT NULL DEFAULT 0,
    risk_control_score      NUMERIC(5, 2)   NOT NULL DEFAULT 0,
    overall_ranking_score   NUMERIC(7, 4)   NOT NULL DEFAULT 0,
    current_streak          INTEGER         NOT NULL DEFAULT 0,
    best_streak             INTEGER         NOT NULL DEFAULT 0,
    last_updated            TIMESTAMP       NOT NULL DEFAULT NOW(),
    created_at              TIMESTAMP       NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_wins_non_negative CHECK (wins >= 0),
    CONSTRAINT chk_losses_non_negative CHECK (losses >= 0),
    CONSTRAINT chk_total_trades_non_negative CHECK (total_trades >= 0),
    CONSTRAINT chk_win_rate_range CHECK (win_rate >= 0 AND win_rate <= 1)
);

-- Index for leaderboard queries (sorted by ranking score descending)
CREATE INDEX idx_trader_stats_ranking ON trader_stats (overall_ranking_score DESC);

-- Index for user lookup
CREATE INDEX idx_trader_stats_user_id ON trader_stats (user_id);

-- Index for filtering by total trades (for min-trades requirement)
CREATE INDEX idx_trader_stats_total_trades ON trader_stats (total_trades);

COMMENT ON TABLE trader_stats IS 'Aggregated trader performance statistics for ranking and leaderboard';
COMMENT ON COLUMN trader_stats.overall_ranking_score IS 'Weighted score: WinRate×0.4 + AvgRR×0.3 + Consistency×0.2 + RiskControl×0.1';
COMMENT ON COLUMN trader_stats.win_rate IS 'Win rate as decimal (0.0 to 1.0)';
