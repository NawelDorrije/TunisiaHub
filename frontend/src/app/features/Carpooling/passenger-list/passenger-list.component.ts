import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { BookingWithContext } from '../../../models/Carpooling/carpooling';
import { CarpoolingDataService } from '../services/carpooling-data.service';

@Component({
  selector: 'app-passenger-list',
  templateUrl: './passenger-list.component.html',
  styleUrls: ['./passenger-list.component.css'],
})
export class PassengerListComponent implements OnInit {
  passengers: BookingWithContext[] = [];
  tripId = 0;
  error = '';
  success = '';
  processingBookingId = 0;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly dataService: CarpoolingDataService,
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.tripId = id;
    this.loadPassengers();
  }

  loadPassengers(): void {
    this.error = '';
    this.dataService.getPassengersForTrip(this.tripId).subscribe({
      next: (passengers) => {
        this.passengers = passengers;
      },
      error: () => {
        this.passengers = [];
        this.error = 'Unable to load passenger list.';
      },
    });
  }

  getPassengerInitials(item: BookingWithContext): string {
    const fullName = item.passenger?.fullName || 'Passenger';

    return fullName
      .split(' ')
      .filter((part) => !!part)
      .slice(0, 2)
      .map((part) => part.charAt(0).toUpperCase())
      .join('');
  }

  getSeatsLabel(item: BookingWithContext): string {
    const seatsBooked = Number(item.booking.seatsBooked || 1);
    return seatsBooked > 1 ? `${seatsBooked} seats booked` : '1 seat booked';
  }

  formatBookingDate(value: string): string {
    return new Date(value).toLocaleDateString('en-GB', {
      day: 'numeric',
      month: 'short',
      year: 'numeric',
    });
  }

  getBookingStatusLabel(item: BookingWithContext): string {
    if (item.booking.status === 'CANCELED') {
      return 'Canceled';
    }

    if (item.booking.status === 'PENDING') {
      return 'Pending';
    }

    return 'Confirmed';
  }

  isPendingBooking(item: BookingWithContext): boolean {
    return item.booking.status === 'PENDING';
  }

  isProcessing(item: BookingWithContext): boolean {
    return this.processingBookingId === item.booking.id;
  }

  approveBooking(item: BookingWithContext): void {
    if (!this.isPendingBooking(item) || this.isProcessing(item)) {
      return;
    }

    this.processingBookingId = item.booking.id;
    this.error = '';
    this.success = '';
    this.dataService.approveBooking(item.booking.id).subscribe({
      next: (result) => {
        this.processingBookingId = 0;
        if (!result.ok) {
          this.error = result.error ?? 'Unable to approve booking.';
          return;
        }

        this.success = 'Booking approved successfully.';
        this.loadPassengers();
      },
      error: () => {
        this.processingBookingId = 0;
        this.error = 'Unable to approve booking.';
      },
    });
  }

  rejectBooking(item: BookingWithContext): void {
    if (!this.isPendingBooking(item) || this.isProcessing(item)) {
      return;
    }

    const confirmed = window.confirm('Reject this booking request?');
    if (!confirmed) {
      return;
    }

    this.processingBookingId = item.booking.id;
    this.error = '';
    this.success = '';
    this.dataService.rejectBooking(item.booking.id).subscribe({
      next: (result) => {
        this.processingBookingId = 0;
        if (!result.ok) {
          this.error = result.error ?? 'Unable to reject booking.';
          return;
        }

        this.success = 'Booking rejected successfully.';
        this.loadPassengers();
      },
      error: () => {
        this.processingBookingId = 0;
        this.error = 'Unable to reject booking.';
      },
    });
  }
}
