import { Injectable, PLATFORM_ID, Inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
<<<<<<< HEAD
import { Observable, tap } from 'rxjs';
import { isPlatformBrowser } from '@angular/common';
import { AuthResponse, LoginRequest, RegisterRequest } from '../../../models/auth/auth.model';
=======
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
>>>>>>> origin/feature/integrated-app-event

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private baseUrl = 'http://localhost:8089/api/auth';
<<<<<<< HEAD
=======
  private isBrowser: boolean;

  private userSubject = new BehaviorSubject<UserState | null>(null);
  public user$ = this.userSubject.asObservable();
>>>>>>> origin/feature/integrated-app-event

  constructor(
    private http: HttpClient,
    @Inject(PLATFORM_ID) private platformId: Object
<<<<<<< HEAD
  ) { }

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
    if (!isPlatformBrowser(this.platformId)) return;
    console.log('DEBUG: AuthService storing user data from response:', response);
    localStorage.setItem('token', response.token);
    if (response.role != null && response.role !== '') {
      localStorage.setItem('role', String(response.role));
    }
    localStorage.setItem('email', response.email);
    localStorage.setItem('nom', response.nom);
    localStorage.setItem('prenom', response.prenom);
    localStorage.setItem('id', String(response.id));
    console.log('DEBUG: Stored ID in localStorage:', localStorage.getItem('id'));
  }

  getUserId(): number | null {
    if (!isPlatformBrowser(this.platformId)) return null;
    const id = localStorage.getItem('id');
    return id ? Number(id) : null;
  }

  logout(): void {
    if (isPlatformBrowser(this.platformId)) localStorage.clear();
  }

  getToken(): string | null {
    return isPlatformBrowser(this.platformId) ? localStorage.getItem('token') : null;
  }

  /** Role from JWT when logged in (source of truth), else storage. */
  getRole(): string | null {
    if (!isPlatformBrowser(this.platformId)) return null;
    const token = localStorage.getItem('token');
    const fromJwt = this.getRoleFromJwt(token);
    if (fromJwt) {
      localStorage.setItem('role', fromJwt);
      return fromJwt;
    }
    const stored = localStorage.getItem('role');
    return this.isValidStoredRole(stored) ? stored : null;
  }

  getNom(): string | null {
    return isPlatformBrowser(this.platformId) ? localStorage.getItem('nom') : null;
  }

  getPrenom(): string | null {
    return isPlatformBrowser(this.platformId) ? localStorage.getItem('prenom') : null;
  }

  isLoggedIn(): boolean {
    return this.getToken() !== null;
  }

  isAdmin(): boolean {
    return this.normalizeRole(this.getRole()) === 'ADMIN';
  }

  isClient(): boolean {
    return this.normalizeRole(this.getRole()) === 'CLIENT';
  }

  private isValidStoredRole(value: string | null): boolean {
    if (value == null || value === '' || value === 'undefined' || value === 'null') {
      return false;
    }
    return true;
  }

  /** Match Spring-style ROLE_ADMIN or plain ADMIN. */
  private normalizeRole(role: string | null): string | null {
    if (!role) return null;
    let r = role.trim().toUpperCase();
    if (r.startsWith('ROLE_')) r = r.slice('ROLE_'.length);
    return r || null;
  }

  private getRoleFromJwt(token: string | null): string | null {
    if (!token) return null;
    try {
      const payload = token.split('.')[1];
      if (!payload) return null;
      let base64 = payload.replace(/-/g, '+').replace(/_/g, '/');
      base64 += '='.repeat((4 - (base64.length % 4)) % 4);
      const json = JSON.parse(atob(base64)) as { role?: string };
      return json.role ?? null;
    } catch {
      return null;
    }
=======
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
>>>>>>> origin/feature/integrated-app-event
  }
}