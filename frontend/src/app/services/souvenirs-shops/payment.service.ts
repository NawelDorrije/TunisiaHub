import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Payment } from '../../models/souvenirs-shops/payment.model';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class PaymentService {

  private apiUrl = `${environment.apiUrl}/payments`;

  constructor(private http: HttpClient) {}

  getAllPayments(): Observable<Payment[]> { return this.http.get<Payment[]>(`${this.apiUrl}`); }
  getPaymentById(id: number): Observable<Payment> { return this.http.get<Payment>(`${this.apiUrl}/${id}`); }
  getPaymentByOrder(orderId: number): Observable<Payment> { return this.http.get<Payment>(`${this.apiUrl}/order/${orderId}`); }

  addPayment(payment: Payment): Observable<Payment> { return this.http.post<Payment>(`${this.apiUrl}`, payment); }
  updatePayment(payment: Payment): Observable<Payment> { return this.http.put<Payment>(`${this.apiUrl}`, payment); }
  deletePayment(id: number): Observable<void> { return this.http.delete<void>(`${this.apiUrl}/${id}`); }
}