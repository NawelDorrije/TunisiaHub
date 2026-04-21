import { Component, Input, OnInit, PLATFORM_ID, Inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { ReservationService } from '../../services/reservation.service';
import { AuthService } from '../../../auth/services/auth.service';
import { ReservedDateRange } from '../../../../models/accommodations/reservation.model';

@Component({
  selector: 'app-reservation-form',
  templateUrl: './reservation-form.component.html',
  styleUrls: ['./reservation-form.component.css']
})
export class ReservationFormComponent implements OnInit {

  @Input() accommodationId!: number;
  @Input() pricePerNight!: number;
  @Input() accommodationTitle!: string;

  currentStep: number = 1;
  isLoading = false;
  errorMessage = '';
  reservedRanges: ReservedDateRange[] = [];

  startDate: string = '';
  endDate: string = '';
  nights: number = 0;
  totalPrice: number = 0;
  today: string = new Date().toISOString().split('T')[0];
  minEndDate: string = '';
  // Add these properties
  showFeedback = false;
  confirmedReservationId: number | null = null;

  paymentForm = new FormGroup({
    cardName: new FormControl('', Validators.required),
    cardNumber: new FormControl('', [
      Validators.required,
      Validators.pattern('^[0-9]{16}$')
    ]),
    expiry: new FormControl('', [
      Validators.required,
      Validators.pattern('^(0[1-9]|1[0-2])\\/[0-9]{2}$')
    ]),
    cvv: new FormControl('', [
      Validators.required,
      Validators.pattern('^[0-9]{3}$')
    ])
  });

  get f() { return this.paymentForm.controls; }

  constructor(
    private reservationService: ReservationService,
    public authService: AuthService,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {}

  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      this.loadReservedDates();
    }
  }
loadReservedDates(): void {
  this.reservationService.getReservedDates(this.accommodationId).subscribe({
    next: (data) => {
      const today = new Date();
      today.setHours(0, 0, 0, 0);

      // ← filter out ranges that are completely in the past
      this.reservedRanges = data.filter(range => {
        const endDate = new Date(range.endDate);
        return endDate >= today;
      });
    },
    error: () => {}
  });
}

  isDateReserved(date: string): boolean {
    const d = new Date(date);
    return this.reservedRanges.some(range => {
      const start = new Date(range.startDate);
      const end = new Date(range.endDate);
      return d >= start && d <= end;
    });
  }

  onStartDateChange(): void {
    this.endDate = '';
    this.nights = 0;
    this.totalPrice = 0;
    this.errorMessage = '';

    if (this.isDateReserved(this.startDate)) {
      this.errorMessage = 'This date is already reserved. Please pick another date.';
      this.startDate = '';
      return;
    }

    // Set minimum end date to day after start
    const next = new Date(this.startDate);
    next.setDate(next.getDate() + 1);
    this.minEndDate = next.toISOString().split('T')[0];
  }

  onEndDateChange(): void {
    this.errorMessage = '';

    if (this.isDateReserved(this.endDate)) {
      this.errorMessage = 'This date is already reserved. Please pick another date.';
      this.endDate = '';
      return;
    }

    // Check if any date in range is reserved
    const start = new Date(this.startDate);
    const end = new Date(this.endDate);
    let current = new Date(start);

    while (current <= end) {
      const dateStr = current.toISOString().split('T')[0];
      if (this.isDateReserved(dateStr)) {
        this.errorMessage = 'Some dates in your selected range are already reserved.';
        this.endDate = '';
        return;
      }
      current.setDate(current.getDate() + 1);
    }

    // Calculate nights and price
    const diff = end.getTime() - start.getTime();
    this.nights = Math.round(diff / (1000 * 60 * 60 * 24));
    this.totalPrice = this.nights * this.pricePerNight;
  }

  goToPayment(): void {
    if (!this.startDate || !this.endDate || this.nights <= 0) {
      this.errorMessage = 'Please select valid check-in and check-out dates.';
      return;
    }
    this.currentStep = 2;
  }
  
  confirmReservation(): void {
    if (this.paymentForm.invalid) {
      this.paymentForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    this.reservationService.addReservation(this.accommodationId, {
      startDate: this.startDate,
      endDate: this.endDate
    }).subscribe({
     next: (response: any) => {
    this.isLoading = false;
    this.currentStep = 3;
    this.confirmedReservationId = response.id; // ← save reservation id
    this.loadReservedDates();

  // Show feedback popup after 2 seconds
  setTimeout(() => {
    this.showFeedback = true;
    }, 2000);
   },
      error: (err) => {
        this.errorMessage = err.error || 'Reservation failed. Please try again.';
        this.isLoading = false;
      }
    });
  }
  onFeedbackClosed(): void {
  this.showFeedback = false;
}

  resetForm(): void {
    this.currentStep = 1;
    this.startDate = '';
    this.endDate = '';
    this.nights = 0;
    this.totalPrice = 0;
    this.errorMessage = '';
    this.paymentForm.reset();
  }
}