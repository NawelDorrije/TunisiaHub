import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Trip } from '../../../models/Carpooling/carpooling';
import { CarpoolingDataService } from '../services/carpooling-data.service';

@Component({
  selector: 'app-trip-details',
  templateUrl: './trip-details.component.html',
  styleUrls: ['./trip-details.component.css'],
})
export class TripDetailsComponent implements OnInit {
  trip?: Trip;
  driverName = '';
  isOwner = false;
  error = '';

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly dataService: CarpoolingDataService,
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!Number.isFinite(id)) {
      this.error = 'Invalid trip id.';
      return;
    }

    this.dataService.getTripById(id).subscribe({
      next: (trip) => {
        if (!trip) {
          this.error = 'Trip not found.';
          return;
        }

        this.trip = trip;
        this.isOwner =
          this.dataService.getCurrentUser().id === trip.ownerUserId;
        this.driverName = this.dataService.getUserById(
          trip.ownerUserId,
        ).fullName;
      },
      error: () => {
        this.error = 'Unable to load trip details from backend.';
      },
    });
  }

  editTrip(): void {
    if (this.trip) {
      this.router.navigate(['/carpooling/trip', this.trip.id, 'edit']);
    }
  }

  cancelTrip(): void {
    if (!this.trip) {
      return;
    }

    const confirmed = window.confirm('Cancel this trip?');
    if (!confirmed) {
      return;
    }

    this.dataService.cancelTrip(this.trip.id).subscribe({
      next: (result: { ok: boolean; error?: string }) => {
        if (!result.ok) {
          this.error = result.error ?? 'Unable to cancel trip.';
          return;
        }
        this.dataService.getTripById(this.trip!.id).subscribe((trip) => {
          this.trip = trip;
        });
      },
      error: () => {
        this.error = 'Unable to cancel trip.';
      },
    });
  }

  viewPassengers(): void {
    if (this.trip) {
      this.router.navigate(['/carpooling/trip', this.trip.id, 'passengers']);
    }
  }

  bookSeats(): void {
    if (this.trip) {
      this.router.navigate(['/carpooling/trip', this.trip.id, 'book']);
    }
  }

  reportRide(): void {
    if (this.trip) {
      this.router.navigate(['/carpooling/report-complaint'], {
        queryParams: { tripId: this.trip.id },
      });
    }
  }
}
