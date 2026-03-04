package com.investtrack.common.enums;

/**
 * Represents the lifecycle status of a trade idea.
 * <ul>
 *   <li>OPEN — Trade is active, being tracked against live prices</li>
 *   <li>TARGET_HIT — LTP reached the target price (win)</li>
 *   <li>SL_HIT — LTP reached the stop-loss price (loss)</li>
 *   <li>EXPIRED — Trade expired without hitting target or stop-loss</li>
 * </ul>
 */
public enum TradeStatus {
    OPEN,
    TARGET_HIT,
    SL_HIT,
    EXPIRED
}
