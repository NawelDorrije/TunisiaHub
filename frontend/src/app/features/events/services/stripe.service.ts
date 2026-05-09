import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class StripeService {

  private api = 'http://localhost:8089/stripe';

  constructor(private http: HttpClient) {}

  createPaymentIntent(reservationId: number, amount: number) {
    return this.http.post<any>(
      `${this.api}/create-payment-intent`,
      {
        reservationId: reservationId,
        amount: amount
      }
    );
  }
}