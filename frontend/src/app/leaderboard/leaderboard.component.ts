import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { AnalyticsService } from '../services/analytics.service';
import { TraderRanking } from '../shared/models/analytics.model';

@Component({
  selector: 'app-leaderboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    MatTableModule,
    MatPaginatorModule,
    MatCardModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatTooltipModule
  ],
  templateUrl: './leaderboard.component.html',
  styles: [`
    .leaderboard-container {
      max-width: 1100px;
      margin: 0 auto;
      padding: 24px;
    }

    h1 {
      font-size: 28px;
      font-weight: 400;
      color: var(--text-primary);
      margin: 0 0 8px;
    }

    .subtitle {
      color: var(--text-secondary);
      font-size: 14px;
      margin-bottom: 24px;
    }

    .table-container {
      background: var(--surface);
      border-radius: var(--border-radius);
      box-shadow: var(--shadow-sm);
      overflow-x: auto;
    }

    table {
      width: 100%;
    }

    .mat-mdc-row:hover {
      background-color: #f5f5f5;
      cursor: pointer;
    }

    .rank-cell {
      font-weight: 600;
      width: 60px;
      text-align: center;
    }

    .rank-1 { color: #ffd700; }
    .rank-2 { color: #c0c0c0; }
    .rank-3 { color: #cd7f32; }

    .rank-medal {
      font-size: 20px;
    }

    .username-cell {
      font-weight: 500;
      color: var(--primary);
    }

    .score-cell {
      font-weight: 600;
      font-size: 15px;
    }

    .win-rate-bar {
      display: flex;
      align-items: center;
      gap: 8px;

      .bar-track {
        flex: 1;
        height: 6px;
        background: #e0e0e0;
        border-radius: 3px;
        overflow: hidden;
        max-width: 100px;

        .bar-fill {
          height: 100%;
          border-radius: 3px;
          transition: width 0.3s ease;
        }
      }

      .bar-label {
        font-size: 13px;
        min-width: 40px;
      }
    }

    .streak-badge {
      display: inline-flex;
      align-items: center;
      gap: 4px;
      font-size: 13px;

      mat-icon {
        font-size: 16px;
        width: 16px;
        height: 16px;
      }

      &.streak-positive {
        color: var(--success);
      }

      &.streak-negative {
        color: var(--warn);
      }

      &.streak-zero {
        color: var(--text-secondary);
      }
    }

    .loading-container {
      display: flex;
      justify-content: center;
      padding: 48px;
    }

    .empty-state {
      text-align: center;
      padding: 48px;
      color: var(--text-secondary);

      mat-icon {
        font-size: 64px;
        width: 64px;
        height: 64px;
        opacity: 0.4;
        margin-bottom: 16px;
      }
    }
  `]
})
export class LeaderboardComponent implements OnInit {

  displayedColumns = ['rank', 'username', 'totalTrades', 'winRate', 'avgRiskReward', 'currentStreak', 'overallRankingScore'];

  traders = signal<TraderRanking[]>([]);
  totalElements = signal(0);
  isLoading = signal(true);

  pageIndex = 0;
  pageSize = 20;

  constructor(private analyticsService: AnalyticsService) {}

  ngOnInit(): void {
    this.loadLeaderboard();
  }

  loadLeaderboard(): void {
    this.isLoading.set(true);
    this.analyticsService.getLeaderboard(this.pageIndex, this.pageSize).subscribe({
      next: (page) => {
        this.traders.set(page.content);
        this.totalElements.set(page.totalElements);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
      }
    });
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadLeaderboard();
  }

  getRankDisplay(index: number): string {
    const rank = this.pageIndex * this.pageSize + index + 1;
    if (rank === 1) return '🥇';
    if (rank === 2) return '🥈';
    if (rank === 3) return '🥉';
    return `#${rank}`;
  }

  getWinRateColor(winRate: number): string {
    if (winRate >= 60) return 'var(--success)';
    if (winRate >= 40) return 'var(--info)';
    return 'var(--warn)';
  }

  getStreakClass(streak: number): string {
    if (streak > 0) return 'streak-positive';
    if (streak < 0) return 'streak-negative';
    return 'streak-zero';
  }
}
