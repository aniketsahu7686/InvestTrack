import { Component, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { TradeService } from '../../services/trade.service';

@Component({
  selector: 'app-create-trade',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule
  ],
  templateUrl: './create-trade.component.html',
  styles: [`
    .create-trade-container {
      max-width: 650px;
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

    .trade-form {
      display: flex;
      flex-direction: column;
      gap: 8px;
    }

    .price-row {
      display: grid;
      grid-template-columns: 1fr 1fr 1fr;
      gap: 16px;
    }

    .meta-row {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 16px;
    }

    .rr-preview {
      margin: 8px 0 16px;
      padding: 12px 16px;
      border-radius: 8px;
      font-size: 14px;
      font-weight: 500;
      display: flex;
      align-items: center;
      gap: 8px;

      &.rr-valid {
        background-color: var(--success-light);
        color: var(--success);
      }

      &.rr-invalid {
        background-color: var(--warn-light);
        color: var(--warn);
      }

      &.rr-neutral {
        background-color: #f5f5f5;
        color: var(--text-secondary);
      }
    }

    .error-message {
      color: var(--warn);
      font-size: 13px;
      text-align: center;
      margin-bottom: 8px;
      padding: 8px;
      background-color: var(--warn-light);
      border-radius: 4px;
    }

    .form-actions {
      display: flex;
      justify-content: flex-end;
      gap: 12px;
      margin-top: 16px;
    }

    @media (max-width: 600px) {
      .price-row, .meta-row {
        grid-template-columns: 1fr;
      }
    }
  `]
})
export class CreateTradeComponent {

  tradeForm: FormGroup;
  isLoading = signal(false);
  errorMessage = signal<string | null>(null);

  timeframes = ['INTRADAY', 'SWING', 'POSITIONAL'];
  reasons = [
    { value: 'TECHNICAL', label: 'Technical Analysis' },
    { value: 'FUNDAMENTAL', label: 'Fundamental Analysis' },
    { value: 'NEWS_BASED', label: 'News Based' },
    { value: 'EARNINGS_PLAY', label: 'Earnings Play' },
    { value: 'SECTOR_MOMENTUM', label: 'Sector Momentum' },
    { value: 'CONTRARIAN', label: 'Contrarian' },
    { value: 'BREAKOUT', label: 'Breakout' },
    { value: 'OTHER', label: 'Other' }
  ];

  calculatedRR = computed(() => {
    const form = this.tradeForm;
    if (!form) return null;
    const entry = form.get('entryPrice')?.value;
    const sl = form.get('stopLoss')?.value;
    const target = form.get('targetPrice')?.value;

    if (entry && sl && target && entry > sl && target > entry) {
      const risk = entry - sl;
      const reward = target - entry;
      return reward / risk;
    }
    return null;
  });

  constructor(
    private fb: FormBuilder,
    private tradeService: TradeService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {
    this.tradeForm = this.fb.group({
      stockSymbol: ['', [Validators.required, Validators.minLength(1), Validators.maxLength(20)]],
      entryPrice: [null, [Validators.required, Validators.min(0.01)]],
      stopLoss: [null, [Validators.required, Validators.min(0.01)]],
      targetPrice: [null, [Validators.required, Validators.min(0.01)]],
      timeframe: ['', [Validators.required]],
      reason: ['TECHNICAL', [Validators.required]],
      notes: ['', [Validators.maxLength(1000)]]
    });
  }

  getRRDisplay(): { value: string; class: string; icon: string } {
    const rr = this.calculatedRR();
    if (rr === null) {
      return { value: 'Enter valid prices to calculate R:R', class: 'rr-neutral', icon: 'calculate' };
    }
    if (rr >= 1) {
      return { value: `Risk:Reward = 1:${rr.toFixed(2)}`, class: 'rr-valid', icon: 'check_circle' };
    }
    return { value: `Risk:Reward = 1:${rr.toFixed(2)} (must be ≥ 1:1)`, class: 'rr-invalid', icon: 'warning' };
  }

  onSubmit(): void {
    if (this.tradeForm.invalid) {
      this.tradeForm.markAllAsTouched();
      return;
    }

    const form = this.tradeForm.value;

    // Validate SL < Entry
    if (form.stopLoss >= form.entryPrice) {
      this.errorMessage.set('Stop loss must be below entry price.');
      return;
    }

    // Validate Target > Entry
    if (form.targetPrice <= form.entryPrice) {
      this.errorMessage.set('Target price must be above entry price.');
      return;
    }

    // Validate R:R >= 1
    const risk = form.entryPrice - form.stopLoss;
    const reward = form.targetPrice - form.entryPrice;
    if (reward / risk < 1) {
      this.errorMessage.set('Risk:Reward ratio must be at least 1:1.');
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set(null);

    const request = {
      ...form,
      stockSymbol: form.stockSymbol.toUpperCase()
    };

    this.tradeService.createTrade(request).subscribe({
      next: (created) => {
        this.snackBar.open('Trade idea created successfully!', 'View', {
          duration: 4000,
          horizontalPosition: 'end',
          verticalPosition: 'top'
        }).onAction().subscribe(() => {
          this.router.navigate(['/trades', created.id]);
        });
        this.router.navigate(['/trades']);
      },
      error: (err) => {
        this.isLoading.set(false);
        this.errorMessage.set(err.error?.message || 'Failed to create trade idea. Please try again.');
      }
    });
  }
}
