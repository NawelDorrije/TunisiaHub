import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { BookingWithContext } from '../../../models/Carpooling/carpooling';
import { CarpoolingDataService } from '../services/carpooling-data.service';

@Component({
  selector: 'app-my-bookings',
  templateUrl: './my-bookings.component.html',
  styleUrls: ['./my-bookings.component.css'],
})
export class MyBookingsComponent implements OnInit {
  bookings: BookingWithContext[] = [];
  error = '';

  constructor(
    private readonly dataService: CarpoolingDataService,
    private readonly router: Router,
  ) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.dataService.getMyBookingsWithContext().subscribe({
      next: (bookings) => {
        this.bookings = bookings;
      },
      error: () => {
        this.error = 'Unable to load bookings from backend.';
      },
    });
  }

  viewTripDetails(tripId?: number): void {
    if (tripId) {
      this.router.navigate(['/carpooling/trip', tripId]);
    }
  }

  cancelBooking(bookingId: number): void {
    this.error = '';
    const confirmed = window.confirm('Cancel this booking?');
    if (!confirmed) {
      return;
    }

    this.dataService.cancelBooking(bookingId).subscribe({
      next: (result) => {
        if (!result.ok) {
          this.error = result.error ?? 'Unable to cancel booking.';
          return;
        }
        this.load();
      },
      error: () => {
        this.error = 'Unable to cancel booking.';
      },
    });
  }

  reportIssue(tripId?: number, bookingId?: number): void {
    this.router.navigate(['/carpooling/report-complaint'], {
      queryParams: { tripId, bookingId },
    });
  }
}
