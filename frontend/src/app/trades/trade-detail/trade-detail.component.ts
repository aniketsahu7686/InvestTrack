import { Component, OnInit, signal, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { TradeService } from '../../services/trade.service';
import { TradeIdea } from '../../shared/models/trade.model';

@Component({
  selector: 'app-trade-detail',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatDividerModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './trade-detail.component.html',
  styles: [`
    .trade-detail-container {
      max-width: 800px;
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

    .detail-card {
      margin-bottom: 16px;
    }

    .symbol-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;

      .symbol {
        font-size: 24px;
        font-weight: 500;
        color: var(--primary);
      }
    }

    .price-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
      gap: 16px;
      margin: 24px 0;
    }

    .price-item {
      text-align: center;
      padding: 16px;
      border-radius: 8px;
      background: #f8f9fa;

      .label {
        font-size: 12px;
        color: var(--text-secondary);
        text-transform: uppercase;
        letter-spacing: 0.5px;
        margin-bottom: 4px;
      }

      .value {
        font-size: 20px;
        font-weight: 500;
        color: var(--text-primary);
      }

      &.entry { border-left: 3px solid var(--primary); }
      &.sl { border-left: 3px solid var(--warn); }
      &.target { border-left: 3px solid var(--success); }
      &.rr { border-left: 3px solid var(--accent); }
    }

    .meta-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 16px;
      margin: 16px 0;
    }

    .meta-item {
      .label {
        font-size: 12px;
        color: var(--text-secondary);
        text-transform: uppercase;
        letter-spacing: 0.5px;
        margin-bottom: 4px;
      }

      .value {
        font-size: 15px;
        color: var(--text-primary);
      }
    }

    .notes-section {
      margin-top: 16px;

      h3 {
        font-size: 14px;
        color: var(--text-secondary);
        text-transform: uppercase;
        letter-spacing: 0.5px;
        margin-bottom: 8px;
      }

      .notes-content {
        font-size: 14px;
        line-height: 1.6;
        color: var(--text-primary);
        white-space: pre-wrap;
        background: #f8f9fa;
        padding: 16px;
        border-radius: 8px;
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
export class TradeDetailComponent implements OnInit {

  @Input() id!: string;

  trade = signal<TradeIdea | null>(null);
  isLoading = signal(true);
  notFound = signal(false);

  constructor(private tradeService: TradeService) {}

  ngOnInit(): void {
    if (this.id) {
      this.loadTrade(this.id);
    }
  }

  private loadTrade(id: string): void {
    this.tradeService.getTradeById(id).subscribe({
      next: (trade) => {
        this.trade.set(trade);
        this.isLoading.set(false);
      },
      error: (err) => {
        this.isLoading.set(false);
        if (err.status === 404) {
          this.notFound.set(true);
        }
      }
    });
  }

  getStatusClass(status: string): string {
    return `status-${status.toLowerCase().replace('_', '-')}`;
  }

  getRRClass(rr: number): string {
    if (rr >= 2) return 'rr-good';
    if (rr >= 1) return 'rr-moderate';
    return 'rr-poor';
  }

  formatDateTime(date: string): string {
    return new Date(date).toLocaleString('en-IN', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }
}
