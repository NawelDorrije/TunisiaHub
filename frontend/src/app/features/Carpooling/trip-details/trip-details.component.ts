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
  passengersCount = 0;

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

        if (this.isOwner) {
          this.loadPassengersCount(trip.id);
        }
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

  makeTripAvailable(): void {
    if (!this.trip) {
      return;
    }

    const confirmed = window.confirm('Make this trip available again?');
    if (!confirmed) {
      return;
    }

    this.dataService.makeTripAvailable(this.trip.id).subscribe({
      next: (result: { ok: boolean; error?: string }) => {
        if (!result.ok) {
          this.error = result.error ?? 'Unable to make trip available.';
          return;
        }
        this.dataService.getTripById(this.trip!.id).subscribe((trip) => {
          this.trip = trip;
        });
      },
      error: () => {
        this.error = 'Unable to make trip available.';
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

  isCanceledTrip(): boolean {
    return this.trip?.status === 'CANCELED';
  }

  formatTripLabel(dateTime: string): string {
    const tripDate = new Date(dateTime);
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

  formatArrivalTime(dateTime: string): string {
    const tripDate = new Date(dateTime);
    const arrivalDate = new Date(tripDate.getTime() + this.estimateDurationMinutes() * 60000);
    return this.formatTimeFromDate(arrivalDate);
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

  getPassengersLabel(): string {
    if (this.passengersCount <= 0) {
      return 'No passengers for this trip';
    }

    if (this.passengersCount === 1) {
      return '1 passenger for this trip';
    }

    return `${this.passengersCount} passengers for this trip`;
  }

  private loadPassengersCount(tripId: number): void {
    this.dataService.getPassengersForTrip(tripId).subscribe({
      next: (bookings) => {
        this.passengersCount = bookings.length;
      },
      error: () => {
        this.passengersCount = 0;
      },
    });
  }

  private estimateDurationMinutes(): number {
    if (!this.trip) {
      return 20;
    }

    if (this.trip.durationMinutes && this.trip.durationMinutes > 0) {
      return Math.round(Number(this.trip.durationMinutes));
    }

    const departure = this.formatMainPlace(this.trip.departure).toLowerCase();
    const destination = this.formatMainPlace(this.trip.destination).toLowerCase();

    if (departure === destination) {
      return 10;
    }

    if (
      this.trip.departure.toLowerCase().includes(destination) ||
      this.trip.destination.toLowerCase().includes(departure)
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
}
