import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../features/auth/services/auth.service';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrl: './header.component.css'
})
export class HeaderComponent {

  constructor(
    public authService: AuthService,
    private router: Router
  ) {}

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/auth/sign-in']);
  }
}