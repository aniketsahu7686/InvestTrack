import { Component } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-disclaimer-banner',
  standalone: true,
  imports: [MatIconModule],
  templateUrl: './disclaimer-banner.component.html',
  styles: [`
    .disclaimer-banner {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      padding: 8px 16px;
      background-color: #fff8e1;
      border-bottom: 1px solid #ffe082;
      font-size: 12px;
      color: #795548;
      text-align: center;

      mat-icon {
        font-size: 16px;
        width: 16px;
        height: 16px;
        color: #ff9800;
      }

      .disclaimer-text {
        font-style: italic;
      }
    }
  `]
})
export class DisclaimerBannerComponent {}
