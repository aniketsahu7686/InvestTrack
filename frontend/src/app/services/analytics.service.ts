import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { TraderRanking, TraderStats, PageResponse } from '../shared/models/analytics.model';

@Injectable({
  providedIn: 'root'
})
export class AnalyticsService {

  private readonly API_URL = `${environment.apiUrl}/analytics`;

  constructor(private http: HttpClient) {}

  /**
   * Get the leaderboard with paginated rankings.
   */
  getLeaderboard(page: number = 0, size: number = 20): Observable<PageResponse<TraderRanking>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<PageResponse<TraderRanking>>(`${this.API_URL}/leaderboard`, { params });
  }

  /**
   * Get detailed stats for a specific trader.
   */
  getTraderStats(userId: string): Observable<TraderStats> {
    return this.http.get<TraderStats>(`${this.API_URL}/trader/${userId}`);
  }
}
