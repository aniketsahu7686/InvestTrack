package com.investtrack.common.enums;

/**
 * Represents the timeframe for a trade idea.
 * <ul>
 *   <li>INTRADAY — Same-day trades, closed before market close</li>
 *   <li>SWING — Multi-day to a few weeks holding period</li>
 *   <li>POSITIONAL — Weeks to months holding period</li>
 * </ul>
 */
public enum Timeframe {
    INTRADAY,
    SWING,
    POSITIONAL
}
