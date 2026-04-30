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

import { Camping }   from '../../../../models/campings/camping';
import { Spot }      from '../../../../models/campings/spot';
import { Activity }  from '../../../../models/campings/activity';
import { Equipment } from '../../../../models/campings/equipment';
import { Payment, ReceptionPaymentMethod } from '../../../../models/shared-reservation/payment';

import { CampingService }     from '../../../../services/campings/camping.service';
import { SpotService }        from '../../../../services/campings/spot.service';
import { ActivityService }    from '../../../../services/campings/activity.service';
import { EquipmentService }   from '../../../../services/campings/equipment.service';
import { ReservationService } from '../../../../services/shared-reservation/reservation-camping.service';
import { PaymentService }     from '../../../../services/shared-reservation/payment.service';
import { AuthService }        from '../../../auth/services/auth.service';

// ── Validators ───────────────────────────────────────────────────────────────

export function minTodayValidator(): ValidatorFn {
  return (c: AbstractControl): ValidationErrors | null => {
    if (!c.value) return null;
    const today = new Date(); today.setHours(0, 0, 0, 0);
    return new Date(c.value) >= today ? null : { pastDate: true };
  };
}

export function checkOutAfterCheckInValidator(checkInName: string): ValidatorFn {
  return (c: AbstractControl): ValidationErrors | null => {
    if (!c.value || !c.parent) return null;
    const ci = c.parent.get(checkInName)?.value;
    return ci && new Date(c.value) > new Date(ci) ? null : { checkOutBeforeCheckIn: true };
  };
}

export function maxCapacityValidator(maxFn: () => number): ValidatorFn {
  return (c: AbstractControl): ValidationErrors | null => {
    const max = maxFn();
    if (!c.value || !max) return null;
    return +c.value <= max ? null : { exceedsCapacity: { max } };
  };
}

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
  spots: Spot[]      = [];
  activities: Activity[]  = [];
  equipmentList: Equipment[] = [];

  selectedSpot: Spot | null = null;
  spotError = false;
  selectedActivities: Activity[] = [];
  selectedEquipment: { equipment: Equipment; quantity: number }[] = [];

  submitting     = false;
  reservationId: number | null = null;
  success        = false;
  submitError:   string | null = null;
  paymentError:  string | null = null;
  clientId!: number;

  completedPayment: Payment | null = null;
  minimumDepositPercent = 30;

  readonly receptionPaymentMethods: { value: ReceptionPaymentMethod; label: string; icon: string }[] = [
    { value: 'CASH',              label: 'Cash',         icon: '💵' },
    { value: 'CARD_AT_RECEPTION', label: 'Card at desk', icon: '💳' },
  ];

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
    private authService: AuthService,
  ) {}

  // ── Lifecycle ────────────────────────────────────────────────────────────

  ngOnInit(): void {
    const id = this.authService.getUserId();
    if (!id) { this.router.navigate(['/auth/sign-in']); return; }
    this.clientId = id;
    this.buildForms();
    this.loadData();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  // ── Forms ────────────────────────────────────────────────────────────────

  private buildForms(): void {
    this.datesForm = this.fb.group({
      checkIn:        ['', [Validators.required, minTodayValidator()]],
      checkOut:       ['', [Validators.required, checkOutAfterCheckInValidator('checkIn')]],
      numberOfGuests: [1,  [Validators.required, Validators.min(1),
                            maxCapacityValidator(() => this.selectedSpot?.capacity ?? Infinity)]],
      notes:          [''],
    });

    this.datesForm.get('checkIn')!.valueChanges
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => this.datesForm.get('checkOut')!.updateValueAndValidity());

    this.paymentForm = this.fb.group({
      depositPercent:  [this.minimumDepositPercent, [
        Validators.required,
        Validators.min(this.minimumDepositPercent),
        Validators.max(100),
      ]],
      remainingMethod: ['CASH', Validators.required],
    });

    this.paymentForm.get('depositPercent')!.valueChanges
      .pipe(takeUntil(this.destroy$))
      .subscribe((pct: number) => {
        const ctrl = this.paymentForm.get('remainingMethod')!;
        if (pct >= 100) { ctrl.clearValidators(); ctrl.setValue(null); }
        else            { ctrl.setValidators(Validators.required); }
        ctrl.updateValueAndValidity();
      });
  }

  private loadData(): void {
    const campingId = +this.route.snapshot.params['id'];

    this.campingService.getCampingById(campingId).pipe(takeUntil(this.destroy$))
      .subscribe({ next: c => this.camping = c });

    this.spotService.getSpotsByCamping(campingId).pipe(takeUntil(this.destroy$))
      .subscribe({ next: spots => this.spots = spots.filter(s => s.active) });

    this.activityService.getActivitiesByCamping(campingId).pipe(takeUntil(this.destroy$))
      .subscribe({ next: acts => this.activities = acts.filter(a => a.active) });

    this.equipmentService.getEquipmentByCamping(campingId).pipe(takeUntil(this.destroy$))
      .subscribe({ next: equip => this.equipmentList = equip.filter(e => e.available && e.quantity > 0) });
  }

  // ── Step 1 ───────────────────────────────────────────────────────────────

  selectSpot(spot: Spot): void {
    this.selectedSpot = spot;
    this.spotError = false;
    this.datesForm.get('numberOfGuests')!.updateValueAndValidity();
  }

  proceedFromSpotStep(): void {
    if (!this.selectedSpot) { this.spotError = true; return; }
    this.step = 2;
  }

  // ── Step 2 ───────────────────────────────────────────────────────────────

  get checkIn()        { return this.datesForm.get('checkIn')!; }
  get checkOut()       { return this.datesForm.get('checkOut')!; }
  get numberOfGuests() { return this.datesForm.get('numberOfGuests')!; }
  get today(): string  { return new Date().toISOString().split('T')[0]; }

  proceedFromDatesStep(): void {
    this.datesForm.markAllAsTouched();
    if (this.datesForm.invalid) return;
    this.step = 3;
  }

  // ── Step 3 ───────────────────────────────────────────────────────────────

  toggleEquipment(equip: Equipment, qty: number = 1): void {
    const idx = this.selectedEquipment.findIndex(i => i.equipment.id === equip.id);
    if (idx >= 0) {
      if (qty <= 0) this.selectedEquipment.splice(idx, 1);
      else          this.selectedEquipment[idx].quantity = qty;
    } else if (qty > 0) {
      this.selectedEquipment.push({ equipment: equip, quantity: qty });
    }
  }

  isEquipmentSelected(equip: Equipment): boolean {
    return this.selectedEquipment.some(i => i.equipment.id === equip.id);
  }

  getEquipmentQuantity(equip: Equipment): number {
    return this.selectedEquipment.find(i => i.equipment.id === equip.id)?.quantity ?? 0;
  }

  // ── Step 4 ───────────────────────────────────────────────────────────────

  toggleActivity(act: Activity): void {
    const idx = this.selectedActivities.findIndex(a => a.id === act.id);
    if (idx >= 0) this.selectedActivities.splice(idx, 1);
    else          this.selectedActivities.push(act);
  }

  isActivitySelected(act: Activity): boolean {
    return this.selectedActivities.some(a => a.id === act.id);
  }

  // ── Pricing ──────────────────────────────────────────────────────────────

  get nights(): number {
    const ci = this.datesForm.value.checkIn;
    const co = this.datesForm.value.checkOut;
    if (!ci || !co) return 0;
    return Math.max(1, Math.ceil((new Date(co).getTime() - new Date(ci).getTime()) / 86_400_000));
  }

  get spotTotal(): number {
    return (this.selectedSpot?.dynamicPrice ?? 0) * this.nights;
  }

  get activitiesTotal(): number {
    return this.selectedActivities.reduce((s, a) => s + a.price, 0);
  }

  get totalPrice(): number {
    return this.spotTotal + this.activitiesTotal;
  }

  get depositPercent(): number {
    return this.paymentForm?.get('depositPercent')?.value ?? this.minimumDepositPercent;
  }

  get depositPreview(): number {
    return Math.ceil(this.totalPrice * this.depositPercent / 100);
  }

  get remainingPreview(): number {
    return this.totalPrice - this.depositPreview;
  }

  get isFullPayment(): boolean {
    return this.depositPercent >= 100;
  }

  // ── Step 4 → créer la réservation ────────────────────────────────────────

  submitReservation(): void {
    this.datesForm.markAllAsTouched();
    if (this.datesForm.invalid || !this.selectedSpot) return;

    this.submitting  = true;
    this.submitError = null;

    const payload = {
      userId:   this.clientId,
      spotId:   this.selectedSpot.id!,
      activityIds: this.selectedActivities.map(a => a.id!),
      equipmentRequests: this.selectedEquipment.map(i => ({
        equipmentId: i.equipment.id!,
        quantity: i.quantity,
      })),
      checkIn:        this.datesForm.value.checkIn,
      checkOut:       this.datesForm.value.checkOut,
      numberOfGuests: this.datesForm.value.numberOfGuests,
      notes:          this.datesForm.value.notes,
    };

    this.reservationService.createReservation(payload)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: res => {
          this.reservationId = res.id!;
          this.submitting = false;
          this.step = 5;            // → aller à l'étape paiement
        },
        error: () => {
          this.submitting  = false;
          this.submitError = 'Reservation could not be created. Please try again.';
        },
      });
  }

  // ── Step 5 → callbacks Stripe ─────────────────────────────────────────────

  /** Appelé par (paymentSuccess) de app-stripe-payment */
  onStripeSuccess(payment: Payment): void {
    this.completedPayment = payment;
    this.success = true;
  }

  /** Appelé par (paymentError) de app-stripe-payment */
  onStripeError(message: string): void {
    this.paymentError = message;
  }

  // ── Navigation ───────────────────────────────────────────────────────────

  goBack(): void {
    if (this.step > 1) this.step = (this.step - 1) as Step;
  }
}
