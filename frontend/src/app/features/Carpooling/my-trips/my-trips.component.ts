import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
<<<<<<< HEAD
import { Trip } from '../models';
=======
import { Trip } from '../../../models/Carpooling/carpooling';
>>>>>>> origin/feature/integrated-app-event
import { CarpoolingDataService } from '../services/carpooling-data.service';

@Component({
  selector: 'app-my-trips',
  templateUrl: './my-trips.component.html',
  styleUrls: ['./my-trips.component.css'],
})
export class MyTripsComponent implements OnInit {
  trips: Trip[] = [];
<<<<<<< HEAD
  error = '';
=======
  displayedTrips: Trip[] = [];
  error = '';
  currentPage = 1;
  readonly pageSize = 10;
>>>>>>> origin/feature/integrated-app-event

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
<<<<<<< HEAD
=======
        this.currentPage = 1;
        this.updateDisplayedTrips();
>>>>>>> origin/feature/integrated-app-event
      },
      error: () => {
        this.error = 'Unable to load trips from backend.';
      },
    });
  }

<<<<<<< HEAD
=======
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
    this.updateDisplayedTrips();
  }

  nextPage(): void {
    if (!this.hasNextPage()) {
      return;
    }

    this.currentPage += 1;
    this.updateDisplayedTrips();
  }

  getPaginationLabel(): string {
    if (this.trips.length === 0) {
      return '0 of 0';
    }

    const start = (this.currentPage - 1) * this.pageSize + 1;
    const end = Math.min(this.currentPage * this.pageSize, this.trips.length);
    return `${start}-${end} of ${this.trips.length}`;
  }

>>>>>>> origin/feature/integrated-app-event
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
<<<<<<< HEAD
=======

  completeTrip(tripId: number): void {
    this.error = '';
    const confirmed = window.confirm('Mark this trip as completed?');
    if (!confirmed) {
      return;
    }

    this.dataService.completeTrip(tripId).subscribe({
      next: (result) => {
        if (!result.ok) {
          this.error = result.error ?? 'Unable to complete trip.';
          return;
        }
        this.loadTrips();
      },
      error: () => {
        this.error = 'Unable to complete trip.';
      },
    });
  }

  canCompleteTrip(trip: Trip): boolean {
    return trip.status !== 'CANCELED' && trip.status !== 'COMPLETED';
  }

  formatTripLabel(dateTime: string): string {
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

  formatTripTime(dateTime: string): string {
    const tripDate = new Date(dateTime);
    return this.formatTimeFromDate(tripDate);
  }

  formatArrivalTime(trip: Trip): string {
    const startDate = new Date(trip.departureDateTime);
    const durationMinutes = this.estimateDurationMinutes(trip);
    const endDate = new Date(startDate.getTime() + durationMinutes * 60000);
    return this.formatTimeFromDate(endDate);
  }

  isArrivalNextDay(trip: Trip): boolean {
    const startDate = new Date(trip.departureDateTime);
    const durationMinutes = this.estimateDurationMinutes(trip);
    const endDate = new Date(startDate.getTime() + durationMinutes * 60000);

    return !this.isSameDay(startDate, endDate);
  }

  formatDurationLabel(trip: Trip): string {
    const durationMinutes = this.estimateDurationMinutes(trip);
    const hours = Math.floor(durationMinutes / 60);
    const minutes = durationMinutes % 60;

    return `${hours}h${`${minutes}`.padStart(2, '0')}`;
  }

  formatMainPlace(value: string): string {
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

  formatPlaceDetails(value: string): string {
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

  private updateDisplayedTrips(): void {
    const startIndex = (this.currentPage - 1) * this.pageSize;
    const endIndex = startIndex + this.pageSize;
    this.displayedTrips = this.trips.slice(startIndex, endIndex);
  }

  private getTotalPages(): number {
    return Math.ceil(this.trips.length / this.pageSize);
  }
>>>>>>> origin/feature/integrated-app-event
}
