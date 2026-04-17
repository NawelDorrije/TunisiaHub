import { Component, Input, OnInit } from '@angular/core';
import { loadStripe } from '@stripe/stripe-js';
import { StripeService } from '../../services/stripe.service';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-stripe-payment',
  templateUrl: './stripe-payment.component.html',
  styleUrls: ['./stripe-payment.component.css']
})
export class StripePaymentComponent implements OnInit {

  @Input() reservationId!: number;
  @Input() amount!: number;

  stripe: any;
  elements: any;

  cardNumber: any;
  cardExpiry: any;
  cardCvc: any;

  clientSecret!: string;

  show = true;
  loading = false;
  errorMessage = '';
  postalCode = '';

  constructor(
    private stripeService: StripeService,
    private http: HttpClient
  ) {}

 async ngOnInit() {

  if (!this.reservationId) {
    this.errorMessage = "Reservation ID missing ❌";
    return;
  }

  this.stripe = await loadStripe('pk_test_51TJg2RHUNUFSod92tW2RxPBHymLtzObWJrfucLAZxXqg0OmQmR7FiNfD8kgPCrA4APSGJUwKy82mqE87zJkjtqYD00pCTRcPit');

  this.stripeService.createPaymentIntent(this.reservationId, this.amount)
    .subscribe({
      next: (res) => {

        this.clientSecret = res.clientSecret;

        this.elements = this.stripe.elements();

        const style = {
          base: {
            fontSize: '16px',
            color: '#333'
          }
        };

        this.cardNumber = this.elements.create('cardNumber', { style });
        this.cardExpiry = this.elements.create('cardExpiry', { style });
        this.cardCvc = this.elements.create('cardCvc', { style });

        setTimeout(() => {
          this.cardNumber.mount('#card-number');
          this.cardExpiry.mount('#card-expiry');
          this.cardCvc.mount('#card-cvc');
        }, 300);
      },

      error: () => {
        this.errorMessage = "Error initializing payment";
      }
    });
}

  async pay() {

  if (!this.clientSecret) {
    this.errorMessage = "❌ Payment not initialized";
    return;
  }

  this.loading = true;
  this.errorMessage = '';

  const result = await this.stripe.confirmCardPayment(this.clientSecret, {
    payment_method: {
      card: this.cardNumber,
      billing_details: {
        address: {
          postal_code: this.postalCode
        }
      }
    }
  });

  if (result.error) {
    this.errorMessage = result.error.message;
    this.loading = false;
    return;
  }

  // ✅ SAVE PAYMENT
  this.http.post("http://localhost:8089/payment/pay", {
    reservationId: this.reservationId,
    amount: this.amount
  }).subscribe({

    next: () => {

      // 🔥 CONFIRM RESERVATION
      this.http.post(
        `http://localhost:8089/api/reservations/confirm/${this.reservationId}`,
        {}
      ).subscribe();

      // 🔥 EMAIL
      this.http.get(
        `http://localhost:8089/email/send?reservationId=${this.reservationId}`
      ).subscribe();

      alert("✅ Payment successful");

      this.close();
      window.location.href = "/events";
    },

    error: () => {
      this.errorMessage = "Payment failed";
    }
  });
}

  close() {
    this.show = false;
  }
}