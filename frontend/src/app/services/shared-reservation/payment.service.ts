import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Payment } from '../../models/shared-reservation/payment';

@Injectable({ providedIn: 'root' })
export class PaymentService {
  private API_URL = 'http://localhost:8089/api/payments';

  constructor(private http: HttpClient) {}

  processPayment(reservationId: number, method: string): Observable<Payment> {
    return this.http.post<Payment>(`${this.API_URL}/pay/${reservationId}`, null, {
      params: new HttpParams().set('method', method)
    });
  }

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
