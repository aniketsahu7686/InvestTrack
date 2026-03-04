import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, RouterLink, ActivatedRoute } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './login.component.html',
  styles: [`
    .login-container {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: calc(100vh - 128px);
      padding: 24px;
    }

    .login-card {
      width: 100%;
      max-width: 420px;
    }

    .login-header {
      text-align: center;
      margin-bottom: 24px;

      h2 {
        margin: 0 0 8px;
        color: var(--primary);
        font-weight: 500;
      }

      p {
        color: var(--text-secondary);
        font-size: 14px;
      }
    }

    .login-form {
      display: flex;
      flex-direction: column;
      gap: 8px;
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

    .login-actions {
      display: flex;
      flex-direction: column;
      gap: 12px;
      margin-top: 8px;
    }

    .register-link {
      text-align: center;
      margin-top: 16px;
      font-size: 14px;
      color: var(--text-secondary);

      a {
        color: var(--primary);
        text-decoration: none;
        font-weight: 500;

        &:hover {
          text-decoration: underline;
        }
      }
    }
  `]
})
export class LoginComponent {

  loginForm: FormGroup;
  isLoading = signal(false);
  errorMessage = signal<string | null>(null);
  hidePassword = signal(true);

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.loginForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3)]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  onSubmit(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set(null);

    this.authService.login(this.loginForm.value).subscribe({
      next: () => {
        const returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/trades';
        this.router.navigateByUrl(returnUrl);
      },
      error: (err) => {
        this.isLoading.set(false);
        if (err.status === 401) {
          this.errorMessage.set('Invalid username or password.');
        } else if (err.status === 0) {
          this.errorMessage.set('Unable to connect to server. Please try again later.');
        } else {
          this.errorMessage.set(err.error?.message || 'Login failed. Please try again.');
        }
      }
    });
  }
}
