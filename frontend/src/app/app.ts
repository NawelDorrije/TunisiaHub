import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { SessionService } from './core/services/session.service';
import { UserRole } from './features/carpooling/models/trip.model';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, FormsModule],
  templateUrl: './app.html',
  styleUrls: ['./app.css'],
})
export class App {
  protected readonly roleOptions: UserRole[] = ['PASSENGER', 'DRIVER', 'ADMIN'];

  protected readonly navLinks = [
    { label: 'Home', path: '' },
    { label: 'Trips', path: 'carpooling/trips' },
    { label: 'My Trips', path: 'carpooling/my-trips' },
    { label: 'Create Trip', path: 'carpooling/trips/new' },
  ];

  protected readonly footerLinks = [
    { label: 'Home', path: '' },
    { label: 'Trips', path: 'carpooling/trips' },
  ];

  protected readonly currentYear = new Date().getFullYear();

  constructor(private readonly sessionService: SessionService) {}

  protected get userId(): number {
    return this.sessionService.userId();
  }

  protected get role(): UserRole {
    return this.sessionService.role();
  }

  protected onUserIdChange(value: string): void {
    const parsed = Number(value);
    if (Number.isFinite(parsed) && parsed > 0) {
      this.sessionService.setUserId(parsed);
    }
  }

  protected onRoleChange(value: string): void {
    if (value === 'DRIVER' || value === 'PASSENGER' || value === 'ADMIN') {
      this.sessionService.setRole(value);
    }
  }
}
