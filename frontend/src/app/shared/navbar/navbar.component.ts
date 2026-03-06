import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatDividerModule } from '@angular/material/divider';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    RouterLinkActive,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    MatDividerModule
  ],
  templateUrl: './navbar.component.html',
  styles: [`
    .navbar {
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      z-index: 1000;
    }

    .brand {
      display: flex;
      align-items: center;
      gap: 8px;
      text-decoration: none;
      color: white;
      font-size: 20px;
      font-weight: 500;

      mat-icon {
        font-size: 28px;
        width: 28px;
        height: 28px;
      }
    }

    .spacer {
      flex: 1;
    }

    .nav-links {
      display: flex;
      gap: 4px;
      align-items: center;
    }

    .nav-link {
      color: rgba(255, 255, 255, 0.8);
      font-size: 14px;

      &.active-link {
        color: white;
        background-color: rgba(255, 255, 255, 0.15);
      }
    }

    .user-menu-trigger {
      color: white;
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 14px;
    }

    .auth-buttons {
      display: flex;
      gap: 8px;
    }

    @media (max-width: 768px) {
      .nav-label {
        display: none;
      }
    }
  `]
})
export class NavbarComponent {

  isAuthenticated = this.authService.isAuthenticated;
  username = this.authService.username;

  constructor(private authService: AuthService) {}

  logout(): void {
    this.authService.logout();
  }
}
