import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { loadStripe, Stripe, StripeCardElement } from '@stripe/stripe-js';

import {
  Payment,
  PaymentMethod,
  QRScanResult,
  ReceptionPaymentMethod
} from '../../models/shared-reservation/payment';


// ─────────────────────────────────────────────────────────────
// DTOs STRIPE
// ─────────────────────────────────────────────────────────────

export interface PaymentIntentRequest {
  reservationId: number;
  depositPercent?: number;
  method: string;
  remainingMethod?: string;
  clientEmail: string;
  totalAmount: number;
}

export interface PaymentIntentResponse {
  clientSecret: string;
  paymentIntentId: string;
  depositAmount: number;
  currency: string;
}

export interface StripeConfirmResponse {
  paymentIntentId: string;
  stripeStatus: string;
  reservation: Payment;
}

export interface PaymentStatusResponse {
  paymentIntentId: string;
  status: string;
  succeeded: boolean;
}


// ─────────────────────────────────────────────────────────────

@Injectable({
  providedIn: 'root'
})
export class PaymentService {

  private readonly API_URL = 'http://localhost:8089/api/payments';

  // Stripe chargé une seule fois
  private stripePromise: Promise<Stripe | null> =
    loadStripe("pk_test_51QTVvtKtpFAVI9EO8iUpNoxS14eNIMAAMsKOkmUG98AmIaWds7aHQ9P7Ha2JUryiRX3stofXJb0OF6ZPcmo9utyr00yEKUBWqj");

  constructor(private http: HttpClient) {}



  // ═══════════════════════════════════════════════════════════
  // STRIPE — FLOW COMPLET
  // ═══════════════════════════════════════════════════════════

  /**
   * STEP 1 — Create PaymentIntent on backend
   */
  createStripePaymentIntent(
    req: PaymentIntentRequest
  ): Observable<PaymentIntentResponse> {

    return this.http.post<PaymentIntentResponse>(
      `${this.API_URL}/stripe/create-payment-intent`,
      req
    );
  }



  /**
   * STEP 2 — Confirm payment with Stripe
   */
  async confirmCardPayment(
    clientSecret: string,
    cardElement: StripeCardElement,
    cardholderName: string
  ): Promise<{
    paymentIntentId: string | null;
    error?: string;
  }> {

    try {

      const stripe = await this.stripePromise;

      if (!stripe) {
        return {
          paymentIntentId: null,
          error: 'Stripe not initialized'
        };
      }

      const result = await stripe.confirmCardPayment(
        clientSecret,
        {
          payment_method: {
            card: cardElement,
            billing_details: {
              name: cardholderName
            }
          }
        }
      );

      if (result.error) {
        return {
          paymentIntentId: null,
          error: result.error.message
        };
      }

      return {
        paymentIntentId: result.paymentIntent?.id ?? null
      };

    } catch (err: any) {

      return {
        paymentIntentId: null,
        error: err.message
      };

    }

  }



  /**
   * STEP 3 — Notify backend after Stripe success
   */
  finalizeStripePayment(
    paymentIntentId: string,
    req: PaymentIntentRequest
  ): Observable<StripeConfirmResponse> {

    return this.http.post<StripeConfirmResponse>(
      `${this.API_URL}/stripe/confirm/${paymentIntentId}`,
      req
    );

  }



  /**
   * Optional — Check payment status
   */
  getStripePaymentStatus(
    paymentIntentId: string
  ): Observable<PaymentStatusResponse> {

    return this.http.get<PaymentStatusResponse>(
      `${this.API_URL}/stripe/status/${paymentIntentId}`
    );

  }



  /**
   * Get Stripe instance
   */
  getStripe(): Promise<Stripe | null> {
    return this.stripePromise;
  }



  // ═══════════════════════════════════════════════════════════
  // CLASSIC PAYMENT (NON-STRIPE)
  // ═══════════════════════════════════════════════════════════

  processDeposit(
    reservationId: number,
    method: PaymentMethod,
    clientEmail: string,
    depositPercent?: number,
    remainingMethod?: ReceptionPaymentMethod
  ): Observable<Payment> {

    let params = new HttpParams()
      .set('method', method)
      .set('clientEmail', clientEmail);

    if (depositPercent != null) {
      params = params.set(
        'depositPercent',
        depositPercent.toString()
      );
    }

    if (remainingMethod) {
      params = params.set(
        'remainingMethod',
        remainingMethod
      );
    }

    return this.http.post<Payment>(
      `${this.API_URL}/deposit/${reservationId}`,
      null,
      { params }
    );

  }



  // ─────────────────────────────────────────────────────────

  scanQRCode(
    token: string
  ): Observable<QRScanResult> {

    const params = new HttpParams()
      .set('token', token);

    return this.http.get<QRScanResult>(
      `${this.API_URL}/scan`,
      { params }
    );

  }



  settleRemainingBalance(
    paymentId: number,
    receptionMethod: ReceptionPaymentMethod
  ): Observable<Payment> {

    const params = new HttpParams()
      .set('receptionMethod', receptionMethod);

    return this.http.post<Payment>(
      `${this.API_URL}/${paymentId}/settle`,
      null,
      { params }
    );

  }



  resendConfirmation(
    paymentId: number,
    email: string
  ): Observable<void> {

    const params = new HttpParams()
      .set('email', email);

    return this.http.post<void>(
      `${this.API_URL}/${paymentId}/resend`,
      null,
      { params }
    );

  }



  getPaymentById(
    id: number
  ): Observable<Payment> {

    return this.http.get<Payment>(
      `${this.API_URL}/${id}`
    );

  }



  getByReservation(
    reservationId: number
  ): Observable<Payment> {

    return this.http.get<Payment>(
      `${this.API_URL}/reservation/${reservationId}`
    );

  }



  getAllPayments(): Observable<Payment[]> {

    return this.http.get<Payment[]>(
      this.API_URL
    );

  }



  refund(
    paymentId: number
  ): Observable<Payment> {

    return this.http.post<Payment>(
      `${this.API_URL}/${paymentId}/refund`,
      null
    );

  }

}
