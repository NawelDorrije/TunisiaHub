import { Injectable } from '@angular/core';
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

    // ✅ Admin can access
    if (this.authService.isAdmin()) {
      return true;
    }

    // ✅ If not logged in → redirect to sign in
    if (!this.authService.isLoggedIn()) {

      this.router.navigate(
        ['/auth/sign-in'],
        {
          queryParams: {
            returnUrl: this.router.url
          }
        }
      );

      return false;
    }

    // ✅ Owner redirect
    if (this.authService.isOwner()) {

      this.router.navigate(['/camping/backoffice/owner']);

      return false;
    }

    // ✅ Normal user redirect
    this.router.navigate(['/events/user/events']);

    return false;
  }
}
