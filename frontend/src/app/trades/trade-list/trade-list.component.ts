import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { TradeService } from '../../services/trade.service';
import { AuthService } from '../../services/auth.service';
import { TradeIdea } from '../../shared/models/trade.model';

@Component({
  selector: 'app-trade-list',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    FormsModule,
    MatTableModule,
    MatPaginatorModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatSelectModule,
    MatInputModule,
    MatCardModule,
    MatChipsModule,
    MatProgressSpinnerModule,
    MatTooltipModule
  ],
  templateUrl: './trade-list.component.html',
  styles: [`
    .trade-list-container {
      max-width: 1200px;
      margin: 0 auto;
      padding: 24px;
    }

    .header-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 24px;
      flex-wrap: wrap;
      gap: 16px;

      h1 {
        margin: 0;
        font-size: 28px;
        font-weight: 400;
        color: var(--text-primary);
      }
    }

    .filters-row {
      display: flex;
      gap: 12px;
      flex-wrap: wrap;
      margin-bottom: 24px;
      align-items: flex-end;
    }

    .filter-field {
      min-width: 150px;
    }

    .symbol-filter {
      max-width: 160px;
    }

    .table-container {
      overflow-x: auto;
      background: var(--surface);
      border-radius: var(--border-radius);
      box-shadow: var(--shadow-sm);
    }

    table {
      width: 100%;
    }

    .mat-mdc-row:hover {
      background-color: #f5f5f5;
      cursor: pointer;
    }

    .symbol-cell {
      font-weight: 500;
      color: var(--primary);
    }

    .rr-value {
      font-weight: 500;
    }

    .empty-state {
      text-align: center;
      padding: 48px 24px;
      color: var(--text-secondary);

      mat-icon {
        font-size: 64px;
        width: 64px;
        height: 64px;
        margin-bottom: 16px;
        opacity: 0.4;
      }

      h3 {
        margin: 0 0 8px;
        font-weight: 400;
      }
    }

    .loading-container {
      display: flex;
      justify-content: center;
      padding: 48px;
    }

    @media (max-width: 768px) {
      .filters-row {
        flex-direction: column;
      }

      .filter-field {
        width: 100%;
      }
    }
  `]
})
export class TradeListComponent implements OnInit {

  displayedColumns = ['stockSymbol', 'entryPrice', 'stopLoss', 'targetPrice', 'riskRewardRatio', 'timeframe', 'reason', 'status', 'createdAt'];

  trades = signal<TradeIdea[]>([]);
  totalElements = signal(0);
  isLoading = signal(true);
  isAuthenticated = this.authService.isAuthenticated;

  // Filter state
  filterTimeframe = '';
  filterStatus = '';
  filterSymbol = '';
  filterReason = '';
  filterMinRR: number | null = null;

  // Pagination
  pageIndex = 0;
  pageSize = 10;

  timeframes = ['INTRADAY', 'SWING', 'POSITIONAL'];
  statuses = ['OPEN', 'TARGET_HIT', 'SL_HIT', 'EXPIRED', 'CLOSED'];
  reasons = ['TECHNICAL', 'FUNDAMENTAL', 'NEWS_BASED', 'EARNINGS_PLAY', 'SECTOR_MOMENTUM', 'CONTRARIAN', 'BREAKOUT', 'OTHER'];

  constructor(
    private tradeService: TradeService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadTrades();
  }

  loadTrades(): void {
    this.isLoading.set(true);
    const filters: any = {};
    if (this.filterTimeframe) filters.timeframe = this.filterTimeframe;
    if (this.filterStatus) filters.status = this.filterStatus;
    if (this.filterSymbol) filters.stockSymbol = this.filterSymbol.toUpperCase();
    if (this.filterReason) filters.reason = this.filterReason;
    if (this.filterMinRR) filters.minRiskReward = this.filterMinRR;

    this.tradeService.getTrades(this.pageIndex, this.pageSize, filters).subscribe({
      next: (page) => {
        this.trades.set(page.content);
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
    this.loadTrades();
  }

  applyFilters(): void {
    this.pageIndex = 0;
    this.loadTrades();
  }

  clearFilters(): void {
    this.filterTimeframe = '';
    this.filterStatus = '';
    this.filterSymbol = '';
    this.filterReason = '';
    this.filterMinRR = null;
    this.pageIndex = 0;
    this.loadTrades();
  }

  getStatusClass(status: string): string {
    return `status-${status.toLowerCase().replace('_', '-')}`;
  }

  getRRClass(rr: number): string {
    if (rr >= 2) return 'rr-good';
    if (rr >= 1) return 'rr-moderate';
    return 'rr-poor';
  }

  formatDate(date: string): string {
    return new Date(date).toLocaleDateString('en-IN', {
      day: '2-digit',
      month: 'short',
      year: 'numeric'
    });
  }
}
