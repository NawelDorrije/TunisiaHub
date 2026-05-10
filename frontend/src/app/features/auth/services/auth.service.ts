import { Injectable, PLATFORM_ID, Inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { isPlatformBrowser } from '@angular/common';

import {
  AuthResponse,
  LoginRequest,
  RegisterRequest,
  UserRole
} from '../../../models/auth/auth.model';

export interface UserState {
  token: string | null;
  role: UserRole | null;
  email: string | null;
  nom: string | null;
  prenom: string | null;
  userId: number | null;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private baseUrl = 'http://localhost:8089/api/auth';
  private isBrowser: boolean;

  private userSubject = new BehaviorSubject<UserState | null>(null);
  public user$ = this.userSubject.asObservable();

  constructor(
    private http: HttpClient,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {

    this.isBrowser = isPlatformBrowser(this.platformId);

    this.initializeUserState();
  }

  // ================= INIT =================

  private initializeUserState(): void {

    if (!this.isBrowser) {
      return;
    }

    const token = localStorage.getItem('token');

    if (!token) {
      return;
    }

    this.userSubject.next({
      token,
      role: localStorage.getItem('role') as UserRole,
      email: localStorage.getItem('email'),
      nom: localStorage.getItem('nom'),
      prenom: localStorage.getItem('prenom'),
      userId: localStorage.getItem('userId')
        ? parseInt(localStorage.getItem('userId')!, 10)
        : null
    });
  }

  // ================= AUTH =================

  login(request: LoginRequest): Observable<AuthResponse> {

    return this.http
      .post<AuthResponse>(`${this.baseUrl}/login`, request)
      .pipe(
        tap((response) => this.storeUser(response))
      );
  }

  register(request: RegisterRequest): Observable<AuthResponse> {

    return this.http
      .post<AuthResponse>(`${this.baseUrl}/register`, request)
      .pipe(
        tap((response) => this.storeUser(response))
      );
  }

  // ================= STORE USER =================

  private storeUser(response: AuthResponse): void {

    if (!this.isBrowser) {
      return;
    }

    // support both backend response formats
    const role = response.user?.role ?? response.role;
    const email = response.user?.email ?? response.email;
    const userId = response.user?.id ?? response.id;

    localStorage.setItem('token', response.token);
    localStorage.setItem('role', role);
    localStorage.setItem('email', email);
    localStorage.setItem('nom', response.nom);
    localStorage.setItem('prenom', response.prenom);

    if (userId !== undefined && userId !== null) {
      localStorage.setItem('userId', String(userId));
    }

    this.userSubject.next({
      token: response.token,
      role: role as UserRole,
      email,
      nom: response.nom,
      prenom: response.prenom,
      userId: userId ?? null
    });
  }

  // ================= LOGOUT =================

  logout(): void {

    if (this.isBrowser) {
      localStorage.clear();
    }

    this.userSubject.next(null);
  }

  // ================= GETTERS =================

  getToken(): string | null {
    return this.userSubject.value?.token || null;
  }

  getRole(): UserRole | null {
    return this.userSubject.value?.role || null;
  }

  getEmail(): string | null {
    return this.userSubject.value?.email || null;
  }

  getUserId(): number | null {

    if (!this.isBrowser) {
      return null;
    }

    const value = localStorage.getItem('userId');

    if (!value) {
      return null;
    }

    const userId = Number(value);

    return Number.isFinite(userId)
      ? userId
      : null;
  }

  getNom(): string | null {
    return this.userSubject.value?.nom || null;
  }

  getPrenom(): string | null {
    return this.userSubject.value?.prenom || null;
  }

  // ================= CHECKS =================

  isLoggedIn(): boolean {
    return !!this.getToken();
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