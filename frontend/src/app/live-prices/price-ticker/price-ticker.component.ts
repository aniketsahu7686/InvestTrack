import { Component, Input, computed, signal, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { WebSocketService, PriceTick } from '../../services/websocket.service';

@Component({
  selector: 'app-price-ticker',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatIconModule],
  templateUrl: './price-ticker.component.html',
  styles: [`
    .ticker-card {
      padding: 16px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      border-radius: 8px;
      transition: all 0.3s ease;
      border-left: 4px solid transparent;

      &.price-up {
        border-left-color: var(--success);
      }

      &.price-down {
        border-left-color: var(--warn);
      }

      &.price-neutral {
        border-left-color: var(--divider);
      }
    }

    .ticker-left {
      .symbol {
        font-size: 16px;
        font-weight: 600;
        color: var(--text-primary);
      }

      .volume {
        font-size: 11px;
        color: var(--text-secondary);
        margin-top: 2px;
      }
    }

    .ticker-right {
      text-align: right;

      .ltp {
        font-size: 18px;
        font-weight: 600;
      }

      .change {
        font-size: 13px;
        display: flex;
        align-items: center;
        justify-content: flex-end;
        gap: 2px;

        mat-icon {
          font-size: 14px;
          width: 14px;
          height: 14px;
        }
      }

      &.up {
        .ltp, .change { color: var(--success); }
      }

      &.down {
        .ltp, .change { color: var(--warn); }
      }

      &.neutral {
        .ltp { color: var(--text-primary); }
        .change { color: var(--text-secondary); }
      }
    }

    .no-data {
      font-size: 13px;
      color: var(--text-secondary);
      font-style: italic;
    }
  `]
})
export class PriceTickerComponent {

  @Input() symbol!: string;

  constructor(private wsService: WebSocketService) {}

  get tick(): PriceTick | undefined {
    return this.wsService.getPrice(this.symbol);
  }

  get direction(): string {
    const t = this.tick;
    if (!t) return 'neutral';
    if (t.change > 0) return 'up';
    if (t.change < 0) return 'down';
    return 'neutral';
  }

  get directionIcon(): string {
    const d = this.direction;
    if (d === 'up') return 'arrow_drop_up';
    if (d === 'down') return 'arrow_drop_down';
    return 'remove';
  }

  formatVolume(vol: number): string {
    if (vol >= 10000000) return `${(vol / 10000000).toFixed(2)} Cr`;
    if (vol >= 100000) return `${(vol / 100000).toFixed(2)} L`;
    if (vol >= 1000) return `${(vol / 1000).toFixed(1)} K`;
    return vol.toString();
  }
}
