import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { AuthService } from './services/auth.service';

@Injectable({
  providedIn: 'root'
})
export class OwnerGuard implements CanActivate {

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  canActivate(): boolean {

    // ===== OWNER or ADMIN allowed =====
    if (this.authService.isOwner() || this.authService.isAdmin()) {
      return true;
    }

    // ===== LOGGED IN but not owner/admin =====
    if (this.authService.isLoggedIn()) {
      this.router.navigate(['/home']); // CLIENT → frontoffice
      return false;
    }

    // ===== NOT LOGGED IN =====
    this.router.navigate(['/auth/sign-in']);
    return false;
  }
}