import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Trip } from '../models';
import { CarpoolingDataService } from '../services/carpooling-data.service';

@Component({
  selector: 'app-my-trips',
  templateUrl: './my-trips.component.html',
  styleUrls: ['./my-trips.component.css'],
})
export class MyTripsComponent implements OnInit {
  trips: Trip[] = [];
  error = '';

  constructor(
    private readonly dataService: CarpoolingDataService,
    private readonly router: Router,
  ) {}

  ngOnInit(): void {
    this.loadTrips();
  }

  loadTrips(): void {
    this.dataService.getMyTrips().subscribe({
      next: (trips) => {
        this.trips = trips;
      },
      error: () => {
        this.error = 'Unable to load trips from backend.';
      },
    });
  }

  openDetails(tripId: number): void {
    this.router.navigate(['/carpooling/trip', tripId]);
  }

  editTrip(tripId: number): void {
    this.router.navigate(['/carpooling/trip', tripId, 'edit']);
  }

  viewPassengers(tripId: number): void {
    this.router.navigate(['/carpooling/trip', tripId, 'passengers']);
  }

  cancelTrip(tripId: number): void {
    this.error = '';
    const confirmed = window.confirm('Cancel this trip?');
    if (!confirmed) {
      return;
    }

    this.dataService.cancelTrip(tripId).subscribe({
      next: (result) => {
        if (!result.ok) {
          this.error = result.error ?? 'Unable to cancel trip.';
          return;
        }
        this.loadTrips();
      },
      error: () => {
        this.error = 'Unable to cancel trip.';
      },
    });
  }
}
