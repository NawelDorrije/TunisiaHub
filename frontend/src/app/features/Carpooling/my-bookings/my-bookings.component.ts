import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import {
  BookingWithContext,
  Trip,
} from '../../../models/Carpooling/carpooling';
import { CarpoolingDataService } from '../services/carpooling-data.service';

@Component({
  selector: 'app-my-bookings',
  templateUrl: './my-bookings.component.html',
  styleUrls: ['./my-bookings.component.css'],
})
export class MyBookingsComponent implements OnInit {
  bookings: BookingWithContext[] = [];
  displayedBookings: BookingWithContext[] = [];
  error = '';
  success = '';
  currentPage = 1;
  readonly pageSize = 10;

  constructor(
    private readonly dataService: CarpoolingDataService,
    private readonly router: Router,
  ) {}

  ngOnInit(): void {
    this.success = history.state?.successMessage || '';
    this.load();
  }

  load(): void {
    this.dataService.getMyBookingsWithContext().subscribe({
      next: (bookings) => {
        this.bookings = bookings;
        this.currentPage = 1;
        this.updateDisplayedBookings();
      },
      error: () => {
        this.error = 'Unable to load bookings from backend.';
      },
    });
  }

  hasPreviousPage(): boolean {
    return this.currentPage > 1;
  }

  hasNextPage(): boolean {
    return this.currentPage < this.getTotalPages();
  }

  previousPage(): void {
    if (!this.hasPreviousPage()) {
      return;
    }

    this.currentPage -= 1;
    this.updateDisplayedBookings();
  }

  nextPage(): void {
    if (!this.hasNextPage()) {
      return;
    }

    this.currentPage += 1;
    this.updateDisplayedBookings();
  }

  getPaginationLabel(): string {
    if (this.bookings.length === 0) {
      return '0 of 0';
    }

    const start = (this.currentPage - 1) * this.pageSize + 1;
    const end = Math.min(
      this.currentPage * this.pageSize,
      this.bookings.length,
    );
    return `${start}-${end} of ${this.bookings.length}`;
  }

  isCanceledBooking(item: BookingWithContext): boolean {
    return item.booking.status === 'CANCELED';
  }

  isConfirmedBooking(item: BookingWithContext): boolean {
    return item.booking.status === 'CONFIRMED';
  }

  cancelBooking(bookingId: number, event?: Event): void {
    event?.stopPropagation();
    this.error = '';
    const booking = this.bookings.find((item) => item.booking.id === bookingId);
    if (booking?.booking.status === 'CANCELED') {
      return;
    }
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

  reportIssue(tripId?: number, bookingId?: number, event?: Event): void {
    event?.stopPropagation();
    this.router.navigate(['/carpooling/report-complaint'], {
      queryParams: { tripId, bookingId },
    });
  }

  reviewDriver(tripId?: number, bookingId?: number, event?: Event): void {
    event?.stopPropagation();
    this.router.navigate(['/carpooling/review-driver'], {
      queryParams: { tripId, bookingId },
    });
  }

  formatTripLabel(dateTime?: string): string {
    if (!dateTime) {
      return 'Upcoming trip';
    }

    const tripDate = new Date(dateTime);
    const today = new Date();
    const tomorrow = new Date();
    tomorrow.setDate(today.getDate() + 1);

    if (this.isSameDay(tripDate, today)) {
      return 'Today';
    }

    if (this.isSameDay(tripDate, tomorrow)) {
      return 'Tomorrow';
    }

    const dayLabels = ['Sun.', 'Mon.', 'Tue.', 'Wed.', 'Thu.', 'Fri.', 'Sat.'];
    const monthLabels = [
      'Jan',
      'Feb',
      'Mar',
      'Apr',
      'May',
      'Jun',
      'Jul',
      'Aug',
      'Sep',
      'Oct',
      'Nov',
      'Dec',
    ];

    return `${dayLabels[tripDate.getDay()]} ${tripDate.getDate()} ${monthLabels[tripDate.getMonth()]}`;
  }

  formatTripTime(dateTime?: string): string {
    if (!dateTime) {
      return '--:--';
    }

    const tripDate = new Date(dateTime);
    return this.formatTimeFromDate(tripDate);
  }

  formatArrivalTime(trip?: Trip): string {
    if (!trip) {
      return '--:--';
    }

    const startDate = new Date(trip.departureDateTime);
    const durationMinutes = this.estimateDurationMinutes(trip);
    const endDate = new Date(startDate.getTime() + durationMinutes * 60000);
    return this.formatTimeFromDate(endDate);
  }

  isArrivalNextDay(trip?: Trip): boolean {
    if (!trip) {
      return false;
    }

    const startDate = new Date(trip.departureDateTime);
    const durationMinutes = this.estimateDurationMinutes(trip);
    const endDate = new Date(startDate.getTime() + durationMinutes * 60000);

    return !this.isSameDay(startDate, endDate);
  }

  formatDurationLabel(trip?: Trip): string {
    if (!trip) {
      return '0h00';
    }

    const durationMinutes = this.estimateDurationMinutes(trip);
    const hours = Math.floor(durationMinutes / 60);
    const minutes = durationMinutes % 60;

    return `${hours}h${`${minutes}`.padStart(2, '0')}`;
  }

  formatMainPlace(value?: string): string {
    if (!value) {
      return 'Trip point';
    }

    const parts = value
      .split(',')
      .map((part) => part.trim())
      .filter((part) => !!part);

    if (parts.length > 0) {
      return parts[0];
    }

    return value;
  }

  formatPlaceDetails(value?: string): string {
    if (!value) {
      return '';
    }

    const parts = value
      .split(',')
      .map((part) => part.trim())
      .filter((part) => !!part);

    if (parts.length <= 1) {
      return '';
    }

    return parts.slice(1).join(', ');
  }

  formatPrice(value?: number): string {
    return Number(value ?? 0).toFixed(2);
  }

  private estimateDurationMinutes(trip: Trip): number {
    if (trip.durationMinutes && trip.durationMinutes > 0) {
      return Math.round(Number(trip.durationMinutes));
    }

    const departure = this.formatMainPlace(trip.departure).toLowerCase();
    const destination = this.formatMainPlace(trip.destination).toLowerCase();

    if (departure === destination) {
      return 10;
    }

    if (
      trip.departure.toLowerCase().includes(destination) ||
      trip.destination.toLowerCase().includes(departure)
    ) {
      return 15;
    }

    return 20;
  }

  private formatTimeFromDate(value: Date): string {
    const hours = `${value.getHours()}`.padStart(2, '0');
    const minutes = `${value.getMinutes()}`.padStart(2, '0');

    return `${hours}:${minutes}`;
  }

  private isSameDay(firstDate: Date, secondDate: Date): boolean {
    return (
      firstDate.getFullYear() === secondDate.getFullYear() &&
      firstDate.getMonth() === secondDate.getMonth() &&
      firstDate.getDate() === secondDate.getDate()
    );
  }

  private updateDisplayedBookings(): void {
    const startIndex = (this.currentPage - 1) * this.pageSize;
    const endIndex = startIndex + this.pageSize;
    this.displayedBookings = this.bookings.slice(startIndex, endIndex);
  }

  private getTotalPages(): number {
    return Math.ceil(this.bookings.length / this.pageSize);
  }
}
