package com.investtrack.common.enums;

/**
 * User roles for role-based access control.
 * <ul>
 *   <li>USER — Standard registered user, can post trade ideas</li>
 *   <li>VERIFIED_TRADER — Trader with verified track record, gets badge</li>
 *   <li>ADMIN — Platform administrator with elevated permissions</li>
 * </ul>
 */
public enum UserRole {
    USER,
    VERIFIED_TRADER,
    ADMIN
}
