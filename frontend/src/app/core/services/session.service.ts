import { Injectable, signal } from '@angular/core';
import { UserRole } from '../../features/carpooling/models/trip.model';

@Injectable({ providedIn: 'root' })
export class SessionService {
  private readonly userIdSignal = signal<number>(this.getStoredUserId());
  private readonly roleSignal = signal<UserRole>(this.getStoredRole());

  userId() {
    return this.userIdSignal();
  }

  role() {
    return this.roleSignal();
  }

  setUserId(userId: number): void {
    this.userIdSignal.set(userId);
    localStorage.setItem('session.userId', String(userId));
  }

  setRole(role: UserRole): void {
    this.roleSignal.set(role);
    localStorage.setItem('session.role', role);
  }

  private getStoredUserId(): number {
    const stored = localStorage.getItem('session.userId');
    const parsed = Number(stored);
    return Number.isFinite(parsed) && parsed > 0 ? parsed : 1;
  }

  private getStoredRole(): UserRole {
    const stored = localStorage.getItem('session.role');
    if (stored === 'DRIVER' || stored === 'PASSENGER' || stored === 'ADMIN') {
      return stored;
    }
    return 'PASSENGER';
  }
}
