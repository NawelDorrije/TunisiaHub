import { Component, OnInit, OnDestroy } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ValidationErrors,
  ValidatorFn,
  Validators,
} from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import { Camping } from '../../../../models/campings/camping';
import { Spot } from '../../../../models/campings/spot';
import { Activity } from '../../../../models/campings/activity';
import { CampingService } from '../../../../services/campings/camping.service';
import { SpotService } from '../../../../services/campings/spot.service';
import { ActivityService } from '../../../../services/campings/activity.service';
import { ReservationService } from '../../../../services/shared-reservation/reservation-camping.service';
import { PaymentService } from '../../../../services/shared-reservation/payment.service';

// ── Custom Validators ────────────────────────────────────────────────────────

/** Date must be today or in the future. */
export function minTodayValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    if (!control.value) return null;
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const selected = new Date(control.value);
    return selected >= today ? null : { pastDate: true };
  };
}

/** Check-out must be strictly after check-in. Depends on a sibling control. */
export function checkOutAfterCheckInValidator(checkInControlName: string): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    if (!control.value) return null;
    const parent = control.parent;
    if (!parent) return null;
    const checkIn = parent.get(checkInControlName)?.value;
    if (!checkIn) return null;
    return new Date(control.value) > new Date(checkIn) ? null : { checkOutBeforeCheckIn: true };
  };
}

/** Number of guests must not exceed the spot's capacity. */
export function maxCapacityValidator(maxCapacity: () => number): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const max = maxCapacity();
    if (!control.value || !max) return null;
    return +control.value <= max ? null : { exceedsCapacity: { max } };
  };
}

// ── Component ────────────────────────────────────────────────────────────────

export type Step = 1 | 2 | 3 | 4;
export type PaymentMethod = 'CREDIT_CARD' | 'PAYPAL' | 'BANK_TRANSFER' | 'CASH';

@Component({
  selector: 'app-reservation-form',
  templateUrl: './reservation-form.component.html',
  styleUrls: ['./reservation-form.component.css'],
})
export class ReservationFormComponent implements OnInit, OnDestroy {
  // ── State ────────────────────────────────────────────────────────────────
  step: Step = 1;
  camping!: Camping;
  spots: Spot[] = [];
  activities: Activity[] = [];

  selectedSpot: Spot | null = null;
  spotError = false; // touched-and-missing spot validation flag

  selectedActivities: Activity[] = [];

  submitting = false;
  reservationId: number | null = null;
  success = false;
  submitError: string | null = null;
  paymentError: string | null = null;

  readonly paymentMethods: { value: PaymentMethod; label: string; icon: string }[] = [
    { value: 'CREDIT_CARD',    label: 'Credit Card',    icon: '💳' },
    { value: 'PAYPAL',         label: 'PayPal',         icon: '🅿️' },
    { value: 'BANK_TRANSFER',  label: 'Bank Transfer',  icon: '🏦' },
    { value: 'CASH',           label: 'Cash',           icon: '💵' },
  ];

  // ── Forms ────────────────────────────────────────────────────────────────
  datesForm!: FormGroup;
  paymentForm!: FormGroup;

  private destroy$ = new Subject<void>();

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private campingService: CampingService,
    private spotService: SpotService,
    private activityService: ActivityService,
    private reservationService: ReservationService,
    private paymentService: PaymentService,
  ) {}

  // ── Lifecycle ────────────────────────────────────────────────────────────

  ngOnInit(): void {
    this.buildForms();
    this.loadData();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  // ── Initialisation helpers ───────────────────────────────────────────────

  private buildForms(): void {
    this.datesForm = this.fb.group(
      {
        checkIn: ['', [Validators.required, minTodayValidator()]],
        checkOut: ['', [Validators.required, checkOutAfterCheckInValidator('checkIn')]],
        numberOfGuests: [
          1,
          [
            Validators.required,
            Validators.min(1),
            maxCapacityValidator(() => this.selectedSpot?.capacity ?? Infinity),
          ],
        ],
        notes: [''],
      },
    );

    // Re-validate checkOut whenever checkIn changes
    this.datesForm
      .get('checkIn')!
      .valueChanges.pipe(takeUntil(this.destroy$))
      .subscribe(() => this.datesForm.get('checkOut')!.updateValueAndValidity());

    // Re-validate numberOfGuests whenever the selected spot changes
    // (triggered in selectSpot())

    this.paymentForm = this.fb.group({
      method: ['CREDIT_CARD', Validators.required],
    });
  }

  private loadData(): void {
    const campingId = +this.route.snapshot.params['id'];

    this.campingService
      .getCampingById(campingId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({ next: (c) => (this.camping = c) });

    this.spotService
      .getSpotsByCamping(campingId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({ next: (spots) => (this.spots = spots.filter((s) => s.active)) });

    this.activityService
      .getActivitiesByCamping(campingId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({ next: (acts) => (this.activities = acts.filter((a) => a.active)) });
  }

  // ── Step 1 – Spot selection ──────────────────────────────────────────────

  selectSpot(spot: Spot): void {
    this.selectedSpot = spot;
    this.spotError = false;
    // Re-run capacity validator with the new spot
    this.datesForm.get('numberOfGuests')!.updateValueAndValidity();
  }

  /** Advance from step 1; show inline error if no spot chosen. */
  proceedFromSpotStep(): void {
    if (!this.selectedSpot) {
      this.spotError = true;
      return;
    }
    this.step = 2;
  }

  // ── Step 2 – Dates & Guests ──────────────────────────────────────────────

  get checkIn() { return this.datesForm.get('checkIn')!; }
  get checkOut() { return this.datesForm.get('checkOut')!; }
  get numberOfGuests() { return this.datesForm.get('numberOfGuests')!; }

  /** ISO date string for today — used as the `min` attribute on date inputs. */
  get today(): string {
    return new Date().toISOString().split('T')[0];
  }

  /** Advance from step 2 only when all date controls are valid. */
  proceedFromDatesStep(): void {
    this.datesForm.markAllAsTouched();
    if (this.datesForm.invalid) return;
    this.step = 3;
  }

  // ── Step 3 – Activities ──────────────────────────────────────────────────

  toggleActivity(activity: Activity): void {
    const idx = this.selectedActivities.findIndex((a) => a.id === activity.id);
    if (idx >= 0) {
      this.selectedActivities.splice(idx, 1);
    } else {
      this.selectedActivities.push(activity);
    }
  }

  isActivitySelected(activity: Activity): boolean {
    return this.selectedActivities.some((a) => a.id === activity.id);
  }

  // ── Pricing ──────────────────────────────────────────────────────────────

  get nights(): number {
    const ci = this.datesForm.value.checkIn;
    const co = this.datesForm.value.checkOut;
    if (!ci || !co) return 0;
    return Math.max(1, Math.ceil((new Date(co).getTime() - new Date(ci).getTime()) / 86_400_000));
  }

  get activitiesTotal(): number {
    return this.selectedActivities.reduce((sum, a) => sum + a.price, 0);
  }

  get spotTotal(): number {
    return (this.selectedSpot?.basePrice ?? 0) * this.nights;
  }

  get totalPrice(): number {
    return this.spotTotal + this.activitiesTotal;
  }

  // ── Submission ───────────────────────────────────────────────────────────

  submitReservation(): void {
    this.datesForm.markAllAsTouched();
    if (this.datesForm.invalid || !this.selectedSpot) return;

    this.submitting = true;
    this.submitError = null;

    const payload = {
        userId: 2, // ✅ AJOUTER
      spotId: this.selectedSpot.id!,
      activityIds: this.selectedActivities.map((a) => a.id!),
      checkIn: this.datesForm.value.checkIn,
      checkOut: this.datesForm.value.checkOut,
      numberOfGuests: this.datesForm.value.numberOfGuests,
      notes: this.datesForm.value.notes,
    };

    this.reservationService
      .createReservation(payload)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (res) => {
          this.reservationId = res.id!;
          this.submitting = false;
          this.step = 4;
        },
        error: () => {
          this.submitting = false;
          this.submitError = 'Reservation could not be created. Please try again.';
        },
      });
  }

  processPayment(): void {
    if (!this.reservationId) return;

    this.submitting = true;
    this.paymentError = null;

    this.paymentService
      .processPayment(this.reservationId, this.paymentForm.value.method)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.submitting = false;
          this.success = true;
        },
        error: () => {
          this.submitting = false;
          this.paymentError = 'Payment failed. Please check your details and try again.';
        },
      });
  }

  // ── Navigation helpers ───────────────────────────────────────────────────

  goBack(): void {
    if (this.step > 1) this.step = (this.step - 1) as Step;
  }
}
