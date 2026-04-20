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
import { Equipment } from '../../../../models/campings/equipment';
import { Payment, OnlinePaymentMethod, ReceptionPaymentMethod } from '../../../../models/shared-reservation/payment';

import { CampingService } from '../../../../services/campings/camping.service';
import { SpotService } from '../../../../services/campings/spot.service';
import { ActivityService } from '../../../../services/campings/activity.service';
import { EquipmentService } from '../../../../services/campings/equipment.service';
import { ReservationService } from '../../../../services/shared-reservation/reservation-camping.service';
import { PaymentService } from '../../../../services/shared-reservation/payment.service';
import { AuthService } from '../../../auth/services/auth.service';

// ── Custom Validators ────────────────────────────────────────────────────────

export function minTodayValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    if (!control.value) return null;
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const selected = new Date(control.value);
    return selected >= today ? null : { pastDate: true };
  };
}

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

export function maxCapacityValidator(maxCapacity: () => number): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const max = maxCapacity();
    if (!control.value || !max) return null;
    return +control.value <= max ? null : { exceedsCapacity: { max } };
  };
}

// ── Types ────────────────────────────────────────────────────────────────────

export type Step = 1 | 2 | 3 | 4 | 5;

// ── Component ────────────────────────────────────────────────────────────────

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
  equipmentList: Equipment[] = [];

  selectedSpot: Spot | null = null;
  spotError = false;
  selectedActivities: Activity[] = [];
  selectedEquipment: { equipment: Equipment; quantity: number }[] = [];

  submitting = false;
  reservationId: number | null = null;
  success = false;
  submitError: string | null = null;
  paymentError: string | null = null;
   clientId!: number;

  /** Payment result from backend */
  completedPayment: Payment | null = null;

  /** Minimum deposit percentage */
  minimumDepositPercent = 30;

  // ── Payment method options ───────────────────────────────────────────────

  readonly onlinePaymentMethods: { value: OnlinePaymentMethod; label: string; icon: string }[] = [
    { value: 'CREDIT_CARD',   label: 'Credit Card',   icon: '💳' },
    { value: 'PAYPAL',        label: 'PayPal',         icon: '🅿' },
    { value: 'BANK_TRANSFER', label: 'Bank Transfer',  icon: '🏦' },
  ];

  readonly receptionPaymentMethods: { value: ReceptionPaymentMethod; label: string; icon: string }[] = [
    { value: 'CASH',              label: 'Cash',           icon: '💵' },
    { value: 'CARD_AT_RECEPTION', label: 'Card at desk',   icon: '💳' },
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
    private equipmentService: EquipmentService,
    private reservationService: ReservationService,
    private paymentService: PaymentService,
        private authService: AuthService
  ) {}

  // ── Lifecycle ────────────────────────────────────────────────────────────

  ngOnInit(): void {
    const id = this.authService.getId();
    if (!id) {
      this.router.navigate(['/auth/sign-in']);
      return;
    }
    this.clientId = id;
    this.buildForms();
    this.loadData();
  }



  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  // ── Form builder ─────────────────────────────────────────────────────────

  private buildForms(): void {
    this.datesForm = this.fb.group({
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
    });

    this.datesForm.get('checkIn')!
      .valueChanges.pipe(takeUntil(this.destroy$))
      .subscribe(() => this.datesForm.get('checkOut')!.updateValueAndValidity());

    this.paymentForm = this.fb.group({
      method:           ['CREDIT_CARD', Validators.required],
      depositPercent:   [this.minimumDepositPercent, [
        Validators.required,
        Validators.min(this.minimumDepositPercent),
        Validators.max(100),
      ]],
      remainingMethod:  ['CASH', Validators.required],
      clientEmail:      ['', [Validators.required, Validators.email]],
    });

    this.paymentForm.get('depositPercent')!
      .valueChanges.pipe(takeUntil(this.destroy$))
      .subscribe((pct: number) => {
        const ctrl = this.paymentForm.get('remainingMethod')!;
        if (pct >= 100) {
          ctrl.clearValidators();
          ctrl.setValue(null);
        } else {
          ctrl.setValidators(Validators.required);
        }
        ctrl.updateValueAndValidity();
      });
  }

  private loadData(): void {
    const campingId = +this.route.snapshot.params['id'];

    this.campingService.getCampingById(campingId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({ next: (c) => (this.camping = c) });

    this.spotService.getSpotsByCamping(campingId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({ next: (spots) => (this.spots = spots.filter((s) => s.active)) });

    this.activityService.getActivitiesByCamping(campingId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({ next: (acts) => (this.activities = acts.filter((a) => a.active)) });

    this.equipmentService.getEquipmentByCamping(campingId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (equip) => {
          this.equipmentList = equip.filter((e) => e.available && e.quantity > 0);
        }
      });
  }

  // ── Step 1 – Spot Selection ──────────────────────────────────────────────

  selectSpot(spot: Spot): void {
    this.selectedSpot = spot;
    this.spotError = false;
    this.datesForm.get('numberOfGuests')!.updateValueAndValidity();
  }

  proceedFromSpotStep(): void {
    if (!this.selectedSpot) {
      this.spotError = true;
      return;
    }
    this.step = 2;
  }

  // ── Step 2 – Dates & Guests ──────────────────────────────────────────────

  get checkIn()        { return this.datesForm.get('checkIn')!; }
  get checkOut()       { return this.datesForm.get('checkOut')!; }
  get numberOfGuests() { return this.datesForm.get('numberOfGuests')!; }

  get today(): string {
    return new Date().toISOString().split('T')[0];
  }

  proceedFromDatesStep(): void {
    this.datesForm.markAllAsTouched();
    if (this.datesForm.invalid) return;
    this.step = 3;
  }

  // ── Step 3 – Equipment ───────────────────────────────────────────────────

  toggleEquipment(equip: Equipment, qty: number = 1): void {
    const idx = this.selectedEquipment.findIndex(item => item.equipment.id === equip.id);

    if (idx >= 0) {
      if (qty <= 0) {
        this.selectedEquipment.splice(idx, 1);
      } else {
        this.selectedEquipment[idx].quantity = qty;
      }
    } else if (qty > 0) {
      this.selectedEquipment.push({ equipment: equip, quantity: qty });
    }
  }

  isEquipmentSelected(equip: Equipment): boolean {
    return this.selectedEquipment.some(item => item.equipment.id === equip.id);
  }

  getEquipmentQuantity(equip: Equipment): number {
    const item = this.selectedEquipment.find(item => item.equipment.id === equip.id);
    return item ? item.quantity : 0;
  }

  // ── Step 4 – Activities ──────────────────────────────────────────────────

  toggleActivity(activity: Activity): void {
    const idx = this.selectedActivities.findIndex((a) => a.id === activity.id);
    if (idx >= 0) this.selectedActivities.splice(idx, 1);
    else          this.selectedActivities.push(activity);
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
    return (this.selectedSpot?.dynamicPrice ?? 0) * this.nights;
  }

  get totalPrice(): number {
    return this.spotTotal + this.activitiesTotal ;
  }

  get depositPreview(): number {
    const pct = this.paymentForm?.get('depositPercent')?.value ?? this.minimumDepositPercent;
    return Math.ceil(this.totalPrice * pct / 100);
  }

  get remainingPreview(): number {
    return this.totalPrice - this.depositPreview;
  }

  get isFullPayment(): boolean {
    return (this.paymentForm?.get('depositPercent')?.value ?? 0) >= 100;
  }

  // ── Submission ───────────────────────────────────────────────────────────

  submitReservation(): void {
    this.datesForm.markAllAsTouched();
    if (this.datesForm.invalid || !this.selectedSpot) return;

    this.submitting = true;
    this.submitError = null;

    const payload = {
      userId: this.clientId, 
      spotId: this.selectedSpot.id!,
      activityIds: this.selectedActivities.map((a) => a.id!),
      equipmentRequests: this.selectedEquipment.map(item => ({
        equipmentId: item.equipment.id!,
        quantity: item.quantity
      })),
      checkIn: this.datesForm.value.checkIn,
      checkOut: this.datesForm.value.checkOut,
      numberOfGuests: this.datesForm.value.numberOfGuests,
      notes: this.datesForm.value.notes,
    };

    this.reservationService.createReservation(payload)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (res) => {
          this.reservationId = res.id!;
          this.submitting = false;
          this.step = 5;
        },
        error: () => {
          this.submitting = false;
          this.submitError = 'Reservation could not be created. Please try again.';
        },
      });
  }

  processPayment(): void {
    this.paymentForm.markAllAsTouched();
    if (!this.reservationId || this.paymentForm.invalid) return;

    this.submitting = true;
    this.paymentError = null;

    const { method, depositPercent, remainingMethod, clientEmail } = this.paymentForm.value;

    this.paymentService
      .processDeposit(
        this.reservationId,
        method,
        clientEmail,
        depositPercent,
        this.isFullPayment ? undefined : remainingMethod,
      )
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (payment) => {
          this.completedPayment = payment;
          this.submitting = false;
          this.success = true;
        },
        error: () => {
          this.submitting = false;
          this.paymentError = 'Payment failed. Please check your details and try again.';
        },
      });
  }

  // ── Navigation ───────────────────────────────────────────────────────────

  goBack(): void {
    if (this.step > 1) this.step = (this.step - 1) as Step;
  }
}
