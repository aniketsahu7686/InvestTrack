import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { TradeIdea, TradeIdeaRequest, PageResponse } from '../shared/models/trade.model';

@Injectable({
  providedIn: 'root'
})
export class TradeService {

  private readonly API_URL = `${environment.apiUrl}/trades`;

  constructor(private http: HttpClient) {}

  /**
   * Get paginated list of trade ideas with optional filters.
   */
  getTrades(
    page: number = 0,
    size: number = 10,
    filters?: {
      timeframe?: string;
      status?: string;
      minRiskReward?: number;
      stockSymbol?: string;
      reason?: string;
    }
  ): Observable<PageResponse<TradeIdea>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (filters) {
      if (filters.timeframe) params = params.set('timeframe', filters.timeframe);
      if (filters.status) params = params.set('status', filters.status);
      if (filters.minRiskReward) params = params.set('minRiskReward', filters.minRiskReward.toString());
      if (filters.stockSymbol) params = params.set('stockSymbol', filters.stockSymbol);
      if (filters.reason) params = params.set('reason', filters.reason);
    }

    return this.http.get<PageResponse<TradeIdea>>(this.API_URL, { params });
  }

  /**
   * Get a single trade idea by ID.
   */
  getTradeById(id: string): Observable<TradeIdea> {
    return this.http.get<TradeIdea>(`${this.API_URL}/${id}`);
  }

  /**
   * Get all trades for a specific user.
   */
  getTradesByUser(userId: string, page: number = 0, size: number = 10): Observable<PageResponse<TradeIdea>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<PageResponse<TradeIdea>>(`${this.API_URL}/user/${userId}`, { params });
  }

  /**
   * Create a new trade idea.
   */
  createTrade(trade: TradeIdeaRequest): Observable<TradeIdea> {
    return this.http.post<TradeIdea>(this.API_URL, trade);
  }
}
