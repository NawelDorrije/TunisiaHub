import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { AuthService } from './services/auth.service';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {

  constructor(private authService: AuthService, private router: Router) {}

  canActivate(): boolean {
    if (this.authService.isLoggedIn()) {
      return true;
    }
<<<<<<< HEAD
    this.router.navigate(['/sign-in']);
    return false;
  }
}
=======
    this.router.navigate(['/auth/sign-in']);
    return false;
  }
}
>>>>>>> origin/feature/integrated-app-event
