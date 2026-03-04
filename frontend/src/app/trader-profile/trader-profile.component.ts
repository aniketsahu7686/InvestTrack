import { Component, OnInit, signal, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTabsModule } from '@angular/material/tabs';
import { AnalyticsService } from '../services/analytics.service';
import { TradeService } from '../services/trade.service';
import { TraderStats } from '../shared/models/analytics.model';
import { TradeIdea } from '../shared/models/trade.model';

@Component({
  selector: 'app-trader-profile',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatDividerModule,
    MatProgressSpinnerModule,
    MatTabsModule
  ],
  templateUrl: './trader-profile.component.html',
  styles: [`
    .profile-container {
      max-width: 900px;
      margin: 0 auto;
      padding: 24px;
    }

    .header-row {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 24px;

      h1 {
        margin: 0;
        font-size: 28px;
        font-weight: 400;
        color: var(--text-primary);
      }
    }

    .stats-overview {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
      gap: 16px;
      margin-bottom: 24px;
    }

    .stat-card {
      text-align: center;
      padding: 20px 16px;
      background: var(--surface);
      border-radius: var(--border-radius);
      box-shadow: var(--shadow-sm);

      .stat-value {
        font-size: 28px;
        font-weight: 600;
        color: var(--text-primary);
        margin-bottom: 4px;
      }

      .stat-label {
        font-size: 12px;
        color: var(--text-secondary);
        text-transform: uppercase;
        letter-spacing: 0.5px;
      }

      &.highlight {
        background: var(--primary);
        .stat-value, .stat-label { color: white; }
      }
    }

    .breakdown-card {
      margin-bottom: 16px;
    }

    .breakdown-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 16px;
      margin-top: 16px;
    }

    .breakdown-item {
      .item-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 8px;

        .label {
          font-size: 13px;
          color: var(--text-secondary);
        }

        .value {
          font-size: 14px;
          font-weight: 500;
        }
      }

      .progress-track {
        height: 6px;
        background: #e0e0e0;
        border-radius: 3px;
        overflow: hidden;

        .progress-fill {
          height: 100%;
          border-radius: 3px;
          transition: width 0.5s ease;
        }
      }
    }

    .trades-section {
      margin-top: 24px;

      h3 {
        font-size: 18px;
        font-weight: 400;
        margin-bottom: 16px;
      }
    }

    .trade-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px 16px;
      border-bottom: 1px solid var(--divider);
      cursor: pointer;
      transition: background 0.15s;

      &:hover {
        background: #f5f5f5;
      }

      .trade-info {
        .symbol {
          font-weight: 500;
          color: var(--primary);
        }

        .meta {
          font-size: 12px;
          color: var(--text-secondary);
          margin-top: 2px;
        }
      }
    }

    .loading-container {
      display: flex;
      justify-content: center;
      padding: 48px;
    }

    .not-found {
      text-align: center;
      padding: 48px;
      color: var(--text-secondary);
    }
  `]
})
export class TraderProfileComponent implements OnInit {

  @Input() userId!: string;

  stats = signal<TraderStats | null>(null);
  recentTrades = signal<TradeIdea[]>([]);
  isLoading = signal(true);
  notFound = signal(false);

  constructor(
    private analyticsService: AnalyticsService,
    private tradeService: TradeService
  ) {}

  ngOnInit(): void {
    if (this.userId) {
      this.loadProfile();
    }
  }

  private loadProfile(): void {
    this.analyticsService.getTraderStats(this.userId).subscribe({
      next: (stats) => {
        this.stats.set(stats);
        this.isLoading.set(false);
        this.loadRecentTrades();
      },
      error: (err) => {
        this.isLoading.set(false);
        if (err.status === 404) {
          this.notFound.set(true);
        }
      }
    });
  }

  private loadRecentTrades(): void {
    this.tradeService.getTradesByUser(this.userId, 0, 10).subscribe({
      next: (page) => {
        this.recentTrades.set(page.content);
      }
    });
  }

  getScoreColor(score: number): string {
    if (score >= 70) return 'var(--success)';
    if (score >= 40) return 'var(--info)';
    return 'var(--warn)';
  }

  getStatusClass(status: string): string {
    return `status-${status.toLowerCase().replace('_', '-')}`;
  }

  formatDate(date: string): string {
    return new Date(date).toLocaleDateString('en-IN', {
      day: '2-digit',
      month: 'short',
      year: 'numeric'
    });
  }
}
