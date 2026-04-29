import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { AuthService } from './services/auth.service';

@Injectable({
  providedIn: 'root'
})
export class AdminGuard implements CanActivate {

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  canActivate(): boolean {
    if (this.authService.isAdmin()) {
      return true;
    }
    if (this.authService.isOwner()) {
      this.router.navigate(['/camping/backoffice/owner']);
    } else {
      this.router.navigate(['/auth/sign-in']);
    }
    return false;
  }
}
