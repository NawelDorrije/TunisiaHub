import { Injectable, PLATFORM_ID, Inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { isPlatformBrowser } from '@angular/common';
import { AuthResponse, LoginRequest, RegisterRequest, UserRole } from '../../../models/auth/auth.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private baseUrl = 'http://localhost:8089/api/auth';
  private isBrowser: boolean;

  constructor(
    private http: HttpClient,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {
    this.isBrowser = isPlatformBrowser(this.platformId);
  }

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/login`, request).pipe(
      tap(response => this.storeUser(response))
    );
  }

  register(request: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/register`, request).pipe(
      tap(response => this.storeUser(response))
    );
  }

  private storeUser(response: AuthResponse): void {
    if (this.isBrowser) {
      localStorage.setItem('token', response.token);
      localStorage.setItem('role', response.role);
      localStorage.setItem('email', response.email);
      localStorage.setItem('nom', response.nom);
      localStorage.setItem('prenom', response.prenom);
    }
  }

  logout(): void {
    if (this.isBrowser) localStorage.clear();
  }

  getToken(): string | null {
    return this.isBrowser ? localStorage.getItem('token') : null;
  }

  getRole(): UserRole | null {
    return this.isBrowser ? (localStorage.getItem('role') as UserRole | null) : null;
  }

  getEmail(): string | null {
    return this.isBrowser ? localStorage.getItem('email') : null;
  }

  getNom(): string | null {
    return this.isBrowser ? localStorage.getItem('nom') : null;
  }

  getPrenom(): string | null {
    return this.isBrowser ? localStorage.getItem('prenom') : null;
  }

  isLoggedIn(): boolean {
    return this.getToken() !== null;
  }

  isAdmin(): boolean {
    return this.getRole() === 'ADMIN';
  }

  isOwner(): boolean {
    return this.getRole() === 'OWNER';
  }

  isClient(): boolean {
    return this.getRole() === 'CLIENT';
  }
}