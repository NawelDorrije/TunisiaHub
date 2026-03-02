import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { SessionService } from '../../../../core/services/session.service';
import { Trip } from '../../models/trip.model';
import { TripService } from '../../services/trip.service';

@Component({
  selector: 'app-my-trips',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './my-trips.component.html',
  styleUrl: './my-trips.component.css',
})
export class MyTripsComponent implements OnInit {
  private readonly tripService = inject(TripService);
  private readonly sessionService = inject(SessionService);

  protected trips: Trip[] = [];
  protected loading = false;
  protected errorMessage = '';

  protected get isDriver(): boolean {
    return this.sessionService.role() === 'DRIVER';
  }

  ngOnInit(): void {
    if (!this.isDriver) {
      return;
    }

    this.loadTrips();
  }

  cancelTrip(id: number): void {
    this.errorMessage = '';

    this.tripService.cancelTrip(id).subscribe({
      next: () => this.loadTrips(),
      error: (error) => {
        this.errorMessage = error?.error?.message ?? 'Failed to cancel trip';
      },
    });
  }

  formatDateTime(value: string): string {
    return new Date(value).toLocaleString();
  }

  private loadTrips(): void {
    this.loading = true;
    this.errorMessage = '';

    this.tripService.getMyTrips().subscribe({
      next: (trips) => {
        this.trips = trips;
        this.loading = false;
      },
      error: (error) => {
        this.errorMessage = error?.error?.message ?? 'Failed to load your trips';
        this.loading = false;
      },
    });
  }
}
