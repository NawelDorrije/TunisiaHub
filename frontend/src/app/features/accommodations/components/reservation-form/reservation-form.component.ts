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
  startDateValue: string = '';
  endDateValue: string = '';
  nights: number = 0;
  totalPrice: number = 0;
  todayDate: Date = this.startOfDay(new Date());
  today: string = this.toIsoDate(this.todayDate);
  minEndDate: string = '';
  reservedDatesSet: Set<string> = new Set<string>();
  isCheckingAvailability = false;
  availabilityMessage = '';
  isRangeAvailable: boolean | null = null;
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
        this.reservedRanges = data.filter(range => {
          const endDate = this.parseIsoDate(range.endDate);
          return endDate >= this.todayDate;
        });

        this.reservedDatesSet.clear();
        this.reservedRanges.forEach(range => {
          let cursor = this.parseIsoDate(range.startDate);
          const end = this.parseIsoDate(range.endDate);

          while (cursor <= end) {
            this.reservedDatesSet.add(this.toIsoDate(cursor));
            cursor = this.addDays(cursor, 1);
          }
        });
      },
      error: () => {}
    });
  }

  isDateReserved(date: string): boolean {
    return this.reservedDatesSet.has(date);
  }

  onStartDateChange(): void {
    if (!this.startDateValue) {
      this.resetSelectionState();
      return;
    }

    this.startDate = this.startDateValue;
    this.endDate = '';
    this.endDateValue = '';
    this.nights = 0;
    this.totalPrice = 0;
    this.errorMessage = '';
    this.availabilityMessage = '';
    this.isRangeAvailable = null;

    if (this.isDateReserved(this.startDate)) {
      this.errorMessage = 'This date is already reserved. Please pick another date.';
      this.startDate = '';
      this.startDateValue = '';
      return;
    }

    // Set minimum end date to day after start
    const next = this.addDays(this.parseIsoDate(this.startDate), 1);
    this.minEndDate = this.toIsoDate(next);
  }

  onEndDateChange(): void {
    if (!this.endDateValue || !this.startDate) {
      this.endDate = '';
      this.nights = 0;
      this.totalPrice = 0;
      this.availabilityMessage = '';
      this.isRangeAvailable = null;
      return;
    }

    this.endDate = this.endDateValue;
    this.errorMessage = '';
    this.availabilityMessage = '';
    this.isRangeAvailable = null;

    if (this.isDateReserved(this.endDate)) {
      this.errorMessage = 'This date is already reserved. Please pick another date.';
      this.endDate = '';
      this.endDateValue = '';
      return;
    }

    // Check if any date in range is reserved
    const start = this.parseIsoDate(this.startDate);
    const end = this.parseIsoDate(this.endDate);
    let current = new Date(start);

    while (current <= end) {
      const dateStr = this.toIsoDate(current);
      if (this.isDateReserved(dateStr)) {
        this.errorMessage = 'Some dates in your selected range are already reserved.';
        this.endDate = '';
        this.endDateValue = '';
        this.nights = 0;
        this.totalPrice = 0;
        return;
      }
      current = this.addDays(current, 1);
    }

    // Calculate nights and price
    const diff = end.getTime() - start.getTime();
    this.nights = Math.round(diff / (1000 * 60 * 60 * 24));
    this.totalPrice = this.nights * this.pricePerNight;

    if (this.nights > 0) {
      this.checkLiveAvailability();
    }
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
    this.resetSelectionState();
    this.nights = 0;
    this.totalPrice = 0;
    this.errorMessage = '';
    this.paymentForm.reset();
  }

  private checkLiveAvailability(): void {
    if (!this.startDate || !this.endDate) return;

    this.isCheckingAvailability = true;
    this.reservationService.checkAvailability(this.accommodationId, this.startDate, this.endDate).subscribe({
      next: (isAvailable) => {
        this.isCheckingAvailability = false;
        this.isRangeAvailable = isAvailable;
        this.availabilityMessage = isAvailable
          ? 'Great news! These dates are available.'
          : 'These dates were just booked. Please choose another range.';

        if (!isAvailable) {
          this.endDate = '';
          this.endDateValue = '';
          this.nights = 0;
          this.totalPrice = 0;
        }
      },
      error: () => {
        this.isCheckingAvailability = false;
        this.isRangeAvailable = null;
        this.availabilityMessage = 'Could not verify live availability right now.';
      }
    });
  }

  private resetSelectionState(): void {
    this.startDate = '';
    this.endDate = '';
    this.startDateValue = '';
    this.endDateValue = '';
    this.minEndDate = '';
    this.availabilityMessage = '';
    this.isRangeAvailable = null;
    this.isCheckingAvailability = false;
  }

  private startOfDay(date: Date): Date {
    const normalized = new Date(date);
    normalized.setHours(0, 0, 0, 0);
    return normalized;
  }

  private addDays(date: Date, days: number): Date {
    const result = new Date(date);
    result.setDate(result.getDate() + days);
    return this.startOfDay(result);
  }

  private parseIsoDate(value: string): Date {
    const [year, month, day] = value.split('-').map(Number);
    return this.startOfDay(new Date(year, month - 1, day));
  }

  private toIsoDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}