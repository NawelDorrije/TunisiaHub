import { Component, OnInit } from '@angular/core';
import { inject } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Trip } from '../models';
import { CarpoolingDataService } from '../services/carpooling-data.service';

@Component({
  selector: 'app-book-trip',
  templateUrl: './book-trip.component.html',
  styleUrls: ['./book-trip.component.css'],
})
export class BookTripComponent implements OnInit {
  private readonly fb = inject(FormBuilder);

  trip?: Trip;
  error = '';
  success = '';

  readonly bookingForm = this.fb.nonNullable.group({
    seats: [1, [Validators.required, Validators.min(1)]],
  });

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly dataService: CarpoolingDataService,
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.dataService.getTripById(id).subscribe({
      next: (trip) => {
        if (!trip) {
          this.error = 'Trip not found.';
          return;
        }

        this.trip = trip;
        this.bookingForm.patchValue({ seats: 1 });
      },
      error: () => {
        this.error = 'Unable to load trip.';
      },
    });
  }

  get totalPrice(): number {
    if (!this.trip) {
      return 0;
    }
    const seats = this.bookingForm.getRawValue().seats;
    return seats * this.trip.pricePerSeat;
  }

  confirmBooking(): void {
    this.error = '';
    this.success = '';

    if (!this.trip) {
      this.error = 'Trip not found.';
      return;
    }

    if (this.bookingForm.invalid) {
      this.bookingForm.markAllAsTouched();
      return;
    }

    const seats = this.bookingForm.getRawValue().seats;
    this.dataService.bookTrip(this.trip.id, seats).subscribe({
      next: (result) => {
        if (!result.ok) {
          this.error = result.error ?? 'Booking failed.';
          return;
        }

        this.success = 'Booking confirmed successfully.';
        this.dataService.getTripById(this.trip!.id).subscribe((trip) => {
          this.trip = trip;
        });
      },
      error: () => {
        this.error = 'Booking failed.';
      },
    });
  }

  cancel(): void {
    this.router.navigate(['/carpooling/trip', this.trip?.id]);
  }
}
