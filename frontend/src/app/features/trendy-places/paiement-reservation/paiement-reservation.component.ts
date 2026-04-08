import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { TrendyPlacesService } from '../../../services/trendy-places.service';

@Component({
  selector: 'app-paiement-reservation',
  templateUrl: './paiement-reservation.component.html',
  styleUrls: ['./paiement-reservation.component.css']
})
export class PaiementReservationComponent implements OnInit {
  reservation: any = null;
  loading = true;
  step: 'recap' | 'paiement' | 'succes' | 'erreur' = 'recap';

  // Formulaire carte (simulation)
  cardNumber = '';
  cardName = '';
  cardExpiry = '';
  cardCvv = '';
  paymentLoading = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private trendyService: TrendyPlacesService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.trendyService.getMesReservations(1).subscribe({
      next: (data) => {
        this.reservation = data.find((r: any) => r.id === id);
        this.loading = false;
        if (!this.reservation) this.step = 'erreur';
      },
      error: () => { this.loading = false; this.step = 'erreur'; }
    });
  }

  formatCardNumber(event: any): void {
    let val = event.target.value.replace(/\D/g, '').substring(0, 16);
    this.cardNumber = val.replace(/(.{4})/g, '$1 ').trim();
  }

  formatExpiry(event: any): void {
    let val = event.target.value.replace(/\D/g, '').substring(0, 4);
    if (val.length >= 2) val = val.substring(0, 2) + '/' + val.substring(2);
    this.cardExpiry = val;
  }

  isFormValid(): boolean {
    return this.cardNumber.replace(/\s/g, '').length === 16
      && this.cardName.trim().length > 2
      && this.cardExpiry.length === 5
      && this.cardCvv.length === 3;
  }

  payer(): void {
    if (!this.isFormValid()) return;
    this.paymentLoading = true;

    // Simulation délai paiement
    setTimeout(() => {
      this.trendyService.simulerPaiement(this.reservation.id).subscribe({
        next: () => {
          this.paymentLoading = false;
          this.step = 'succes';
        },
        error: () => {
          this.paymentLoading = false;
          this.step = 'erreur';
        }
      });
    }, 2000);
  }

  goToReservations(): void {
    this.router.navigate(['/trendy-places/mes-reservations']);
  }
}