import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Trip } from '../../../models/Carpooling/carpooling';
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
}
