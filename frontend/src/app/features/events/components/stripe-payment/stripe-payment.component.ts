import { Component, Input, OnInit } from '@angular/core';
import { loadStripe } from '@stripe/stripe-js';
import { StripeService } from '../../services/stripe.service';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-stripe-payment',
  templateUrl: './stripe-payment.component.html',
  styleUrls: ['./stripe-payment.component.css'],
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
  paymentIntentId!: string;

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

    // ✅ SAME STRIPE ACCOUNT AS BACKEND
    this.stripe = await loadStripe(
      'pk_test_51QTVvtKtpFAVI9EO8iUpNoxS14eNIMAAMsKOkmUG98AmIaWds7aHQ9P7Ha2JUryiRX3stofXJb0OF6ZPcmo9utyr00yEKUBWqj'
    );

    // ✅ CREATE PAYMENT INTENT
    this.stripeService
      .createPaymentIntent(this.reservationId, this.amount)
      .subscribe({

        next: (res: any) => {

          console.log("Stripe Response:", res);

          this.clientSecret = res.clientSecret;
          this.paymentIntentId = res.paymentIntentId;

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

        error: (err) => {

          console.log(err);

          this.errorMessage = "❌ Error initializing payment";
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

    try {

      // ✅ STRIPE PAYMENT
      const result = await this.stripe.confirmCardPayment(
        this.clientSecret,
        {
          payment_method: {
            card: this.cardNumber,
            billing_details: {
              address: {
                postal_code: this.postalCode
              }
            }
          }
        }
      );

      console.log("Stripe Result:", result);

      // ❌ STRIPE ERROR
      if (result.error) {

        this.errorMessage = result.error.message;
        this.loading = false;

        return;
      }

      // ✅ PAYMENT SUCCEEDED
      if (result.paymentIntent &&
          result.paymentIntent.status === 'succeeded') {

        const paymentIntentId = result.paymentIntent.id;

        console.log("PaymentIntent ID:", paymentIntentId);

        // ✅ BACKEND CONFIRMATION
        this.http.post(

          `http://localhost:8089/api/payments/stripe/confirm/${paymentIntentId}`,

          {
            reservationId: this.reservationId,
            depositPercent: 100,
            method: "CREDIT_CARD",
            remainingMethod: "NONE",
            clientEmail: "test@test.com",
            totalAmount: this.amount
          }

        ).subscribe({

          next: (response) => {

            console.log("Backend confirmation:", response);

            this.loading = false;

            alert("✅ Payment successful");

            this.close();

            window.location.href = "/events";
          },

          error: (err) => {

            console.log("Backend Error:", err);

            this.loading = false;

            this.errorMessage =
              err.error?.message ||
              "❌ Backend payment confirmation failed";
          }
        });
      }

    } catch (err) {

      console.log(err);

      this.loading = false;

      this.errorMessage = "❌ Payment failed";
    }
  }

  close() {
    this.show = false;
  }
}