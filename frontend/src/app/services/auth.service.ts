import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap, catchError, throwError } from 'rxjs';
import { environment } from '../../environments/environment';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  fullName: string;
}

export interface AuthResponse {
  token: string;
  username: string;
  userId: string;
  role: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private readonly API_URL = `${environment.apiUrl}/auth`;
  private readonly TOKEN_KEY = 'investtrack_token';
  private readonly USER_KEY = 'investtrack_user';

  /** Reactive signals for auth state */
  private _isAuthenticated = signal<boolean>(this.hasValidToken());
  private _currentUser = signal<AuthResponse | null>(this.getStoredUser());

  readonly isAuthenticated = this._isAuthenticated.asReadonly();
  readonly currentUser = this._currentUser.asReadonly();
  readonly username = computed(() => this._currentUser()?.username ?? '');
  readonly userId = computed(() => this._currentUser()?.userId ?? '');

  constructor(
    private http: HttpClient,
    private router: Router
  ) {}

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API_URL}/login`, request).pipe(
      tap(response => this.handleAuthSuccess(response)),
      catchError(error => {
        console.error('Login failed:', error);
        return throwError(() => error);
      })
    );
  }

  register(request: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API_URL}/register`, request).pipe(
      tap(response => this.handleAuthSuccess(response)),
      catchError(error => {
        console.error('Registration failed:', error);
        return throwError(() => error);
      })
    );
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    this._isAuthenticated.set(false);
    this._currentUser.set(null);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  private handleAuthSuccess(response: AuthResponse): void {
    localStorage.setItem(this.TOKEN_KEY, response.token);
    localStorage.setItem(this.USER_KEY, JSON.stringify(response));
    this._isAuthenticated.set(true);
    this._currentUser.set(response);
  }

  private hasValidToken(): boolean {
    const token = localStorage.getItem(this.TOKEN_KEY);
    if (!token) return false;

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const expirationDate = new Date(payload.exp * 1000);
      return expirationDate > new Date();
    } catch {
      return false;
    }
  }

  private getStoredUser(): AuthResponse | null {
    const stored = localStorage.getItem(this.USER_KEY);
    if (!stored) return null;

    try {
      return JSON.parse(stored) as AuthResponse;
    } catch {
      return null;
    }
  }
}
