import {
  Component, OnInit, OnDestroy, AfterViewInit,
  ElementRef, ViewChild, Input, Output, EventEmitter
} from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Stripe, StripeCardElement } from '@stripe/stripe-js';
import { PaymentService, PaymentIntentRequest } from '../../../../services/shared-reservation/payment.service';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

@Component({
  selector: 'app-stripe-payment',
  templateUrl: './stripe-payment.component.html',
  styleUrls: ['./stripe-payment.component.css'],
})
export class StripePaymentComponent implements OnInit, AfterViewInit, OnDestroy {

  @ViewChild('cardElement') cardElementRef!: ElementRef;

  @Input() reservationId!: number;
  @Input() totalAmount!: number;
  @Input() depositPercent = 30;
  @Input() remainingMethod = 'CASH';

  @Output() paymentSuccess = new EventEmitter<any>();
  @Output() paymentError   = new EventEmitter<string>();

  form!: FormGroup;
  loading   = false;
  cardError: string | null = null;
  status: 'idle' | 'intent' | 'confirming' | 'finalizing' | 'success' | 'error' = 'idle';

  private stripe!: Stripe | null;
  private cardElement!: StripeCardElement;
  private destroy$ = new Subject<void>();

  constructor(
    private fb: FormBuilder,
    private paymentService: PaymentService   // ← même service, pas de doublon
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      clientEmail:    ['', [Validators.required, Validators.email]],
      cardholderName: ['', Validators.required],
    });
  }

  async ngAfterViewInit(): Promise<void> {
    this.stripe = await this.paymentService.getStripe();
    if (!this.stripe) return;

    const elements = this.stripe.elements();
    this.cardElement = elements.create('card', {
      style: {
        base: {
          fontFamily: '"DM Sans", sans-serif',
          fontSize: '15px',
          color: '#1c2b1f',
          '::placeholder': { color: '#9aaa9e' },
        },
        invalid: { color: '#c0392b' },
      },
    });
    this.cardElement.mount(this.cardElementRef.nativeElement);
    this.cardElement.on('change', (e) => {
      this.cardError = e.error?.message ?? null;
    });
  }

  ngOnDestroy(): void {
    this.cardElement?.destroy();
    this.destroy$.next();
    this.destroy$.complete();
  }

  async pay(): Promise<void> {
    this.form.markAllAsTouched();
    if (this.form.invalid || !this.stripe || !this.cardElement) return;

    this.loading   = true;
    this.cardError = null;

    const req: PaymentIntentRequest = {
      reservationId:  this.reservationId,
      depositPercent: this.depositPercent,
      method:         'CREDIT_CARD',
      remainingMethod: this.depositPercent < 100 ? this.remainingMethod : undefined,
      clientEmail:    this.form.value.clientEmail,
      totalAmount:    this.totalAmount,
    };

    // ── Étape 1 : créer le PaymentIntent côté backend ──────────────────────
    this.status = 'intent';
    this.paymentService.createStripePaymentIntent(req)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: async (intent) => {

          // ── Étape 2 : confirmer avec Stripe.js (navigateur → Stripe) ──────
          this.status = 'confirming';
          const { paymentIntentId, error } =
            await this.paymentService.confirmCardPayment(
              intent.clientSecret,
              this.cardElement,
              this.form.value.cardholderName
            );

          if (error || !paymentIntentId) {
            this.cardError = error ?? 'Paiement refusé par la banque.';
            this.status    = 'error';
            this.loading   = false;
            this.paymentError.emit(this.cardError);
            return;
          }

          // ── Étape 3 : finaliser côté backend (QR, email, réservation) ─────
          this.status = 'finalizing';
          this.paymentService.finalizeStripePayment(paymentIntentId, req)
            .pipe(takeUntil(this.destroy$))
            .subscribe({
              next: (result) => {
                this.status  = 'success';
                this.loading = false;
                this.paymentSuccess.emit(result.reservation);
              },
              error: () => {
                this.cardError = 'Paiement reçu mais finalisation échouée. Contactez le support.';
                this.status    = 'error';
                this.loading   = false;
                this.paymentError.emit(this.cardError!);
              },
            });
        },
        error: () => {
          this.cardError = 'Impossible de créer le paiement. Réessayez.';
          this.status    = 'error';
          this.loading   = false;
          this.paymentError.emit(this.cardError!);
        },
      });
  }

  get depositAmount(): number {
    return Math.ceil(this.totalAmount * this.depositPercent / 100);
  }

  get statusLabel(): string {
    return ({
      idle:       '',
      intent:     'Initialisation du paiement…',
      confirming: 'Confirmation avec la banque…',
      finalizing: 'Finalisation de la réservation…',
      success:    'Paiement réussi !',
      error:      'Erreur de paiement',
    } as Record<string, string>)[this.status];
  }
}
