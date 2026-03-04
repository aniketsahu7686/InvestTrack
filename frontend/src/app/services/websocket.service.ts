import { Injectable, signal, OnDestroy } from '@angular/core';
import { environment } from '../../environments/environment';

export interface PriceTick {
  symbol: string;
  ltp: number;
  change: number;
  changePercent: number;
  volume: number;
  timestamp: string;
}

/**
 * WebSocket service for real-time price updates.
 * Connects to the market-data-service via the API Gateway.
 */
@Injectable({
  providedIn: 'root'
})
export class WebSocketService implements OnDestroy {

  private ws: WebSocket | null = null;
  private reconnectTimer: any = null;
  private reconnectAttempts = 0;
  private readonly MAX_RECONNECT_ATTEMPTS = 10;
  private readonly RECONNECT_DELAY_MS = 3000;

  /** Reactive state */
  private _connected = signal(false);
  private _prices = signal<Map<string, PriceTick>>(new Map());
  private _subscribedSymbols = signal<Set<string>>(new Set());

  readonly connected = this._connected.asReadonly();
  readonly prices = this._prices.asReadonly();
  readonly subscribedSymbols = this._subscribedSymbols.asReadonly();

  connect(): void {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      return;
    }

    try {
      this.ws = new WebSocket(environment.wsUrl);

      this.ws.onopen = () => {
        console.log('[WebSocket] Connected');
        this._connected.set(true);
        this.reconnectAttempts = 0;

        // Re-subscribe to any previously subscribed symbols
        const symbols = this._subscribedSymbols();
        if (symbols.size > 0) {
          this.sendMessage({
            type: 'SUBSCRIBE',
            symbols: Array.from(symbols)
          });
        }
      };

      this.ws.onmessage = (event: MessageEvent) => {
        try {
          const data = JSON.parse(event.data);
          if (data.type === 'PRICE_UPDATE' && data.symbol) {
            const tick: PriceTick = {
              symbol: data.symbol,
              ltp: data.ltp,
              change: data.change || 0,
              changePercent: data.changePercent || 0,
              volume: data.volume || 0,
              timestamp: data.timestamp || new Date().toISOString()
            };

            const updatedPrices = new Map(this._prices());
            updatedPrices.set(tick.symbol, tick);
            this._prices.set(updatedPrices);
          }
        } catch (e) {
          console.error('[WebSocket] Failed to parse message:', e);
        }
      };

      this.ws.onclose = (event) => {
        console.log('[WebSocket] Disconnected:', event.reason || 'Unknown reason');
        this._connected.set(false);
        this.attemptReconnect();
      };

      this.ws.onerror = (error) => {
        console.error('[WebSocket] Error:', error);
        this._connected.set(false);
      };
    } catch (e) {
      console.error('[WebSocket] Connection error:', e);
      this._connected.set(false);
      this.attemptReconnect();
    }
  }

  disconnect(): void {
    this.clearReconnectTimer();
    if (this.ws) {
      this.ws.close(1000, 'User disconnected');
      this.ws = null;
    }
    this._connected.set(false);
  }

  subscribe(symbols: string[]): void {
    const updatedSubs = new Set(this._subscribedSymbols());
    symbols.forEach(s => updatedSubs.add(s.toUpperCase()));
    this._subscribedSymbols.set(updatedSubs);

    if (this._connected()) {
      this.sendMessage({
        type: 'SUBSCRIBE',
        symbols: symbols.map(s => s.toUpperCase())
      });
    }
  }

  unsubscribe(symbols: string[]): void {
    const updatedSubs = new Set(this._subscribedSymbols());
    symbols.forEach(s => updatedSubs.delete(s.toUpperCase()));
    this._subscribedSymbols.set(updatedSubs);

    if (this._connected()) {
      this.sendMessage({
        type: 'UNSUBSCRIBE',
        symbols: symbols.map(s => s.toUpperCase())
      });
    }
  }

  getPrice(symbol: string): PriceTick | undefined {
    return this._prices().get(symbol.toUpperCase());
  }

  private sendMessage(message: object): void {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify(message));
    }
  }

  private attemptReconnect(): void {
    if (this.reconnectAttempts >= this.MAX_RECONNECT_ATTEMPTS) {
      console.warn('[WebSocket] Max reconnect attempts reached');
      return;
    }

    this.clearReconnectTimer();
    const delay = this.RECONNECT_DELAY_MS * Math.pow(1.5, this.reconnectAttempts);
    console.log(`[WebSocket] Reconnecting in ${delay}ms (attempt ${this.reconnectAttempts + 1})`);

    this.reconnectTimer = setTimeout(() => {
      this.reconnectAttempts++;
      this.connect();
    }, delay);
  }

  private clearReconnectTimer(): void {
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer);
      this.reconnectTimer = null;
    }
  }

  ngOnDestroy(): void {
    this.disconnect();
  }
}
