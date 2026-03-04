import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-register',
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
  templateUrl: './register.component.html',
  styles: [`
    .register-container {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: calc(100vh - 128px);
      padding: 24px;
    }

    .register-card {
      width: 100%;
      max-width: 460px;
    }

    .register-header {
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

    .register-form {
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

    .register-actions {
      display: flex;
      flex-direction: column;
      gap: 12px;
      margin-top: 8px;
    }

    .login-link {
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
export class RegisterComponent {

  registerForm: FormGroup;
  isLoading = signal(false);
  errorMessage = signal<string | null>(null);
  hidePassword = signal(true);
  hideConfirmPassword = signal(true);

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.registerForm = this.fb.group({
      fullName: ['', [Validators.required, Validators.minLength(2)]],
      username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(30)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]]
    }, { validators: this.passwordMatchValidator });
  }

  private passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
    const password = control.get('password');
    const confirmPassword = control.get('confirmPassword');
    if (password && confirmPassword && password.value !== confirmPassword.value) {
      confirmPassword.setErrors({ passwordMismatch: true });
      return { passwordMismatch: true };
    }
    return null;
  }

  onSubmit(): void {
    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set(null);

    const { confirmPassword, ...registerData } = this.registerForm.value;

    this.authService.register(registerData).subscribe({
      next: () => {
        this.router.navigate(['/trades']);
      },
      error: (err) => {
        this.isLoading.set(false);
        if (err.status === 409) {
          this.errorMessage.set('Username or email already exists.');
        } else if (err.status === 0) {
          this.errorMessage.set('Unable to connect to server. Please try again later.');
        } else {
          this.errorMessage.set(err.error?.message || 'Registration failed. Please try again.');
        }
      }
    });
  }
}
