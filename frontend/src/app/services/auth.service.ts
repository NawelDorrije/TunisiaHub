import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  constructor() {}

  getUserId(): number {
    if (typeof window === 'undefined' || !window.localStorage) return 0;
    const token = localStorage.getItem('token');
    if (!token) return 0;
    const payload = JSON.parse(atob(token.split('.')[1]));
    return payload.userId;
  }

  getRole(): string {
    if (typeof window === 'undefined' || !window.localStorage) return '';
    const token = localStorage.getItem('token');
    if (!token) return '';
    const payload = JSON.parse(atob(token.split('.')[1]));
    return payload.role;
  }

  isAdmin(): boolean {
    return this.getRole() === 'ADMIN';
  }

  logout() {
    if (typeof window !== 'undefined' && window.localStorage) {
      localStorage.removeItem('token');
    }
  }
}