export interface TradeIdea {
  id: string;
  userId: string;
  username?: string;
  stockSymbol: string;
  entryPrice: number;
  stopLoss: number;
  targetPrice: number;
  riskRewardRatio: number;
  timeframe: Timeframe;
  reason: TradeReason;
  status: TradeStatus;
  notes?: string;
  createdAt: string;
  updatedAt: string;
}

export interface TradeIdeaRequest {
  stockSymbol: string;
  entryPrice: number;
  stopLoss: number;
  targetPrice: number;
  timeframe: Timeframe;
  reason: TradeReason;
  notes?: string;
}

export type Timeframe = 'INTRADAY' | 'SWING' | 'POSITIONAL';

export type TradeStatus = 'OPEN' | 'TARGET_HIT' | 'SL_HIT' | 'EXPIRED' | 'CLOSED';

export type TradeReason =
  | 'TECHNICAL'
  | 'FUNDAMENTAL'
  | 'NEWS_BASED'
  | 'EARNINGS_PLAY'
  | 'SECTOR_MOMENTUM'
  | 'CONTRARIAN'
  | 'BREAKOUT'
  | 'OTHER';

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
