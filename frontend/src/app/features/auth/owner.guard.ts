// owner.guard.ts
import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { AuthService } from './services/auth.service';

@Injectable({ providedIn: 'root' })
export class OwnerGuard implements CanActivate {

  constructor(private authService: AuthService, private router: Router) {}

  canActivate(): boolean {
    if (this.authService.isOwner() || this.authService.isAdmin()) {
      return true;
    }
    if (this.authService.isLoggedIn()) {
      this.router.navigate(['/camping']); // CLIENT → frontoffice
    } else {
      this.router.navigate(['/auth/sign-in']);
    }
    return false;
  }
}
