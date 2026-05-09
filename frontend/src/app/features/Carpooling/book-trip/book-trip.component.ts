import { Component, OnInit } from '@angular/core';
<<<<<<< HEAD
import { inject } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Trip } from '../models';
=======
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ReservationQuote, Trip } from '../../../models/Carpooling/carpooling';
>>>>>>> origin/feature/integrated-app-event
import { CarpoolingDataService } from '../services/carpooling-data.service';

@Component({
  selector: 'app-book-trip',
  templateUrl: './book-trip.component.html',
  styleUrls: ['./book-trip.component.css'],
})
export class BookTripComponent implements OnInit {
<<<<<<< HEAD
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
=======
  trip?: Trip;
  quote?: ReservationQuote;
  error = '';
  success = '';
  showPriceDetails = false;
  introMessage =
    "Hello, I just booked your trip. I'd really like to travel with you!";

  bookingForm!: FormGroup;

  constructor(
    private fb: FormBuilder,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly dataService: CarpoolingDataService,
  ) {
    this.bookingForm = this.fb.group({
      seats: [1, [Validators.required, Validators.min(1)]],
      message: [this.introMessage, [Validators.maxLength(400)]],
    });
  }
>>>>>>> origin/feature/integrated-app-event

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.dataService.getTripById(id).subscribe({
      next: (trip) => {
        if (!trip) {
          this.error = 'Trip not found.';
          return;
        }

        this.trip = trip;
<<<<<<< HEAD
        this.bookingForm.patchValue({ seats: 1 });
=======
        this.bookingForm.patchValue({
          seats: 1,
          message: this.introMessage,
        });
        this.refreshQuote();
>>>>>>> origin/feature/integrated-app-event
      },
      error: () => {
        this.error = 'Unable to load trip.';
      },
    });
<<<<<<< HEAD
  }

  get totalPrice(): number {
    if (!this.trip) {
      return 0;
    }
    const seats = this.bookingForm.getRawValue().seats;
    return seats * this.trip.pricePerSeat;
=======

    this.bookingForm.get('seats')?.valueChanges.subscribe(() => {
      this.refreshQuote();
    });
  }

  get totalPrice(): number {
    return this.quote?.totalAmount ?? 0;
  }

  get driverPrice(): number {
    return this.quote?.driverAmount ?? 0;
  }

  get serviceFee(): number {
    return this.quote?.serviceFee ?? 0;
  }

  get bookingTitle(): string {
    if (!this.trip) {
      return 'Check your booking details';
    }

    return this.trip.bookingMode === 'instant'
      ? 'Check your booking details'
      : 'Check your booking request details';
  }

  get bookingNotice(): string {
    if (!this.trip || this.trip.bookingMode === 'instant') {
      return '';
    }

    return 'Your booking will be confirmed once the driver accepts your request.';
  }

  get successMessage(): string {
    if (!this.trip || this.trip.bookingMode === 'instant') {
      return 'Booking confirmed successfully.';
    }

    return 'Booking request sent successfully.';
  }

  get driverLabel(): string {
    if (!this.trip) {
      return 'the driver';
    }

    return this.trip.ownerFullName || 'the driver';
  }

  get seatsLabel(): string {
    const seats =
      this.quote?.seatsRequested || this.bookingForm.getRawValue().seats || 1;
    return seats > 1 ? `For ${seats} passengers` : 'For 1 passenger';
  }

  openPriceDetails(): void {
    this.showPriceDetails = true;
  }

  closePriceDetails(): void {
    this.showPriceDetails = false;
>>>>>>> origin/feature/integrated-app-event
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

<<<<<<< HEAD
        this.success = 'Booking confirmed successfully.';
        this.dataService.getTripById(this.trip!.id).subscribe((trip) => {
          this.trip = trip;
=======
        this.router.navigate(['/carpooling/my-bookings'], {
          state: { successMessage: this.successMessage },
>>>>>>> origin/feature/integrated-app-event
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
<<<<<<< HEAD
=======

  formatTripDate(dateTime: string): string {
    const tripDate = new Date(dateTime);
    return tripDate.toLocaleDateString('en-GB', {
      weekday: 'short',
      day: 'numeric',
      month: 'long',
    });
  }

  formatTripTime(dateTime: string | Date): string {
    const value = dateTime instanceof Date ? dateTime : new Date(dateTime);
    const hours = `${value.getHours()}`.padStart(2, '0');
    const minutes = `${value.getMinutes()}`.padStart(2, '0');
    return `${hours}:${minutes}`;
  }

  formatArrivalTime(): string {
    if (!this.trip) {
      return '';
    }

    const departure = new Date(this.trip.departureDateTime);
    const arrival = new Date(
      departure.getTime() + this.getDurationMinutes() * 60000,
    );
    return this.formatTripTime(arrival);
  }

  isArrivalNextDay(): boolean {
    if (!this.trip) {
      return false;
    }

    const departure = new Date(this.trip.departureDateTime);
    const arrival = new Date(
      departure.getTime() + this.getDurationMinutes() * 60000,
    );

    return this.isNextDay(departure, arrival);
  }

  formatPlace(value: string): string {
    if (!value) {
      return '';
    }

    return value.split(',')[0].trim();
  }

  formatPrice(value: number): string {
    return value.toFixed(2);
  }

  private refreshQuote(): void {
    if (!this.trip) {
      this.quote = undefined;
      return;
    }

    const seats = Number(this.bookingForm.getRawValue().seats || 1);
    if (!Number.isFinite(seats) || seats < 1) {
      this.quote = undefined;
      return;
    }

    this.dataService.getBookingQuote(this.trip.id, seats).subscribe({
      next: (quote) => {
        this.quote = quote;
      },
      error: () => {
        this.quote = undefined;
      },
    });
  }

  private getDurationMinutes(): number {
    if (this.trip?.durationMinutes && this.trip.durationMinutes > 0) {
      return Math.round(this.trip.durationMinutes);
    }

    return 60;
  }

  private isNextDay(departure: Date, arrival: Date): boolean {
    return (
      departure.getFullYear() !== arrival.getFullYear() ||
      departure.getMonth() !== arrival.getMonth() ||
      departure.getDate() !== arrival.getDate()
    );
  }
>>>>>>> origin/feature/integrated-app-event
}
