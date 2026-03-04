import { Component, OnInit, OnDestroy, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatChipsModule } from '@angular/material/chips';
import { MatBadgeModule } from '@angular/material/badge';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { WebSocketService } from '../../services/websocket.service';
import { PriceTickerComponent } from '../price-ticker/price-ticker.component';

@Component({
  selector: 'app-price-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatChipsModule,
    MatBadgeModule,
    MatProgressSpinnerModule,
    PriceTickerComponent
  ],
  templateUrl: './price-dashboard.component.html',
  styles: [`
    .price-dashboard {
      max-width: 1100px;
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

    .connection-status {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 13px;

      .status-dot {
        width: 10px;
        height: 10px;
        border-radius: 50%;
        display: inline-block;

        &.connected { background-color: var(--success); }
        &.disconnected { background-color: var(--warn); }
      }
    }

    .subscribe-row {
      display: flex;
      gap: 12px;
      align-items: flex-end;
      margin-bottom: 24px;
      flex-wrap: wrap;
    }

    .subscribe-input {
      min-width: 200px;
    }

    .quick-add-chips {
      display: flex;
      gap: 8px;
      flex-wrap: wrap;
      margin-bottom: 24px;
    }

    .ticker-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
      gap: 16px;
    }

    .empty-state {
      text-align: center;
      padding: 48px 24px;
      color: var(--text-secondary);

      mat-icon {
        font-size: 64px;
        width: 64px;
        height: 64px;
        opacity: 0.4;
        margin-bottom: 16px;
      }

      h3 {
        font-weight: 400;
        margin-bottom: 8px;
      }
    }

    @media (max-width: 600px) {
      .ticker-grid {
        grid-template-columns: 1fr;
      }
    }
  `]
})
export class PriceDashboardComponent implements OnInit, OnDestroy {

  newSymbol = '';
  isConnected = this.wsService.connected;
  subscribedSymbols = this.wsService.subscribedSymbols;

  // Popular NSE stocks for quick-add
  popularStocks = ['RELIANCE', 'TCS', 'INFY', 'HDFCBANK', 'ICICIBANK', 'SBIN', 'BHARTIARTL', 'ITC', 'KOTAKBANK', 'LT'];

  constructor(private wsService: WebSocketService) {}

  ngOnInit(): void {
    this.wsService.connect();
  }

  ngOnDestroy(): void {
    // Don't disconnect — keep the WebSocket alive for other components
  }

  addSymbol(): void {
    const symbol = this.newSymbol.trim().toUpperCase();
    if (symbol && !this.subscribedSymbols().has(symbol)) {
      this.wsService.subscribe([symbol]);
      this.newSymbol = '';
    }
  }

  quickAdd(symbol: string): void {
    if (!this.subscribedSymbols().has(symbol)) {
      this.wsService.subscribe([symbol]);
    }
  }

  removeSymbol(symbol: string): void {
    this.wsService.unsubscribe([symbol]);
  }

  isQuickAddActive(symbol: string): boolean {
    return this.subscribedSymbols().has(symbol);
  }

  getSubscribedArray(): string[] {
    return Array.from(this.subscribedSymbols());
  }

  reconnect(): void {
    this.wsService.disconnect();
    this.wsService.connect();
  }
}
