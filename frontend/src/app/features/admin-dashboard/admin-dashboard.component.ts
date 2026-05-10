import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../auth/services/auth.service';

@Component({
  selector: 'app-admin-dashboard',
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.css']
})
export class AdminDashboardComponent {
  adminName: string | null;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {
    this.adminName = this.authService.getNom();
  }

  navigateTo(route: string): void {
    this.router.navigate([`/admin-dashboard/${route}`]);
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/']);
  }
}
