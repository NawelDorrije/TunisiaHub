import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Payment, PaymentMethod, QRScanResult, ReceptionPaymentMethod } from '../../models/shared-reservation/payment';

@Injectable({ providedIn: 'root' })
export class PaymentService {
  private readonly API_URL = 'http://localhost:8089/api/payments';

  constructor(private http: HttpClient) {}

  // ── Deposit (replaces processPayment) ──────────────────────────────────────
  /**
   * Pays the deposit to confirm a reservation.
   *
   * @param reservationId   Target reservation
   * @param method          Online payment method (CREDIT_CARD, PAYPAL, BANK_TRANSFER)
   * @param clientEmail     Email address for confirmation + QR code delivery
   * @param depositPercent  Percentage to pay now (null = server default, usually 30%)
   * @param remainingMethod How remaining balance will be settled (required when depositPercent < 100)
   */
  processDeposit(
    reservationId: number,
    method: PaymentMethod,
    clientEmail: string,
    depositPercent?: number,
    remainingMethod?: ReceptionPaymentMethod,
  ): Observable<Payment> {
    let params = new HttpParams()
      .set('method', method)
      .set('clientEmail', clientEmail);

    if (depositPercent != null) {
      params = params.set('depositPercent', depositPercent);
    }
    if (remainingMethod) {
      params = params.set('remainingMethod', remainingMethod);
    }

    return this.http.post<Payment>(
      `${this.API_URL}/deposit/${reservationId}`,
      null,
      { params },
    );
  }

  // ── Reception ─────────────────────────────────────────────────────────────

  /**
   * Scans a client's check-in QR code at reception.
   * Returns reservation details and automatically marks the reservation as ACTIVE.
   */
  scanQRCode(token: string): Observable<QRScanResult> {
    const params = new HttpParams().set('token', token);
    return this.http.get<QRScanResult>(`${this.API_URL}/scan`, { params });
  }

  /**
   * Marks the remaining balance as settled at reception.
   */
  settleRemainingBalance(
    paymentId: number,
    receptionMethod: ReceptionPaymentMethod,
  ): Observable<Payment> {
    const params = new HttpParams().set('receptionMethod', receptionMethod);
    return this.http.post<Payment>(
      `${this.API_URL}/${paymentId}/settle`,
      null,
      { params },
    );
  }

  /**
   * Resends the confirmation email + QR code to the given address.
   */
  resendConfirmation(paymentId: number, email: string): Observable<void> {
    const params = new HttpParams().set('email', email);
    return this.http.post<void>(
      `${this.API_URL}/${paymentId}/resend`,
      null,
      { params },
    );
  }

  // ── Queries ───────────────────────────────────────────────────────────────

  getPaymentById(id: number): Observable<Payment> {
    return this.http.get<Payment>(`${this.API_URL}/${id}`);
  }

  getByReservation(reservationId: number): Observable<Payment> {
    return this.http.get<Payment>(`${this.API_URL}/reservation/${reservationId}`);
  }

  getAllPayments(): Observable<Payment[]> {
    return this.http.get<Payment[]>(this.API_URL);
  }

  refund(paymentId: number): Observable<Payment> {
    return this.http.post<Payment>(`${this.API_URL}/${paymentId}/refund`, null);
  }
}
