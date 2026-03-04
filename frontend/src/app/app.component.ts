import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { NavbarComponent } from './shared/navbar/navbar.component';
import { DisclaimerBannerComponent } from './shared/disclaimer-banner/disclaimer-banner.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, NavbarComponent, DisclaimerBannerComponent],
  templateUrl: './app.component.html',
  styles: [`
    .app-container {
      display: flex;
      flex-direction: column;
      min-height: 100vh;
    }

    .main-content {
      flex: 1;
      padding-top: 64px; /* toolbar height */
    }
  `]
})
export class AppComponent {
  title = 'InvestTrack';
}
