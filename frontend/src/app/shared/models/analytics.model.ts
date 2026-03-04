export interface TraderRanking {
  userId: string;
  username: string;
  totalTrades: number;
  wins: number;
  losses: number;
  winRate: number;
  avgRiskReward: number;
  overallRankingScore: number;
  currentStreak: number;
  bestStreak: number;
}

export interface TraderStats {
  userId: string;
  username: string;
  totalTrades: number;
  wins: number;
  losses: number;
  expired: number;
  winRate: number;
  avgRiskReward: number;
  consistencyScore: number;
  riskControlScore: number;
  overallRankingScore: number;
  currentStreak: number;
  bestStreak: number;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}
