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
  isTranche = false; // ← true si on arrive depuis paiement-tranche

  step: 'choix' | 'recap' | 'paiement' | 'succes' | 'erreur' = 'choix';

  modePaiement: 'TOTAL' | 'TRANCHE' = 'TOTAL';
  nombreTranches: 2 | 3 = 2;
  paiementResult: any = null;

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

    // Détecter si on vient de paiement-tranche
    this.isTranche = this.route.snapshot.routeConfig?.path?.includes('paiement-tranche') || false;

    this.trendyService.getMesReservations(1).subscribe({
      next: (data) => {
        this.reservation = data.find((r: any) => r.id === id);
        this.loading = false;

        if (!this.reservation) {
          this.step = 'erreur';
          return;
        }

        // Si on paie une tranche → aller directement au paiement
        if (this.isTranche) {
          this.step = 'paiement';
        }
      },
      error: () => { this.loading = false; this.step = 'erreur'; }
    });
  }

  getMontantTranche(): number {
    if (!this.reservation) return 0;
    if (this.isTranche) {
      // Calculer la tranche restante
      const nb = this.reservation.nombreTranches || 2;
      return Math.round((this.reservation.prixTotal / nb) * 100) / 100;
    }
    return Math.round((this.reservation.prixTotal / this.nombreTranches) * 100) / 100;
  }

  getMontantRestantApresTranche(): number {
    if (!this.reservation) return 0;
    return Math.round((this.reservation.prixTotal - this.getMontantTranche()) * 100) / 100;
  }

  getMontantAPayer(): number {
    if (this.isTranche) return this.getMontantTranche();
    return this.modePaiement === 'TOTAL' ? this.reservation?.prixTotal : this.getMontantTranche();
  }

  continuerVersRecap(): void { this.step = 'recap'; }
  continuerVersPaiement(): void { this.step = 'paiement'; }

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

    setTimeout(() => {
      if (this.isTranche) {
        // Payer la prochaine tranche
        this.trendyService.payerTranche(this.reservation.id).subscribe({
          next: (result) => {
            this.paiementResult = result;
            this.paymentLoading = false;
            this.step = 'succes';
          },
          error: () => {
            this.paymentLoading = false;
            this.step = 'erreur';
          }
        });
      } else {
        // Premier paiement
        this.trendyService.payerReservation(
          this.reservation.id,
          this.modePaiement,
          this.modePaiement === 'TRANCHE' ? this.nombreTranches : undefined
        ).subscribe({
          next: (result) => {
            this.paiementResult = result;
            this.paymentLoading = false;
            this.step = 'succes';
          },
          error: () => {
            this.paymentLoading = false;
            this.step = 'erreur';
          }
        });
      }
    }, 2000);
  }

  // Pour afficher le bon message succès
  isPayementComplet(): boolean {
    return this.paiementResult?.paiementComplet === true ||
           this.paiementResult?.statut === 'CONFIRMEE';
  }

  getNumTranchePaye(): number {
    return this.paiementResult?.trancheActuelle || 1;
  }

  getNombreTranchesTotal(): number {
    return this.reservation?.nombreTranches || this.paiementResult?.nombreTranches || 2;
  }

  getMontantRestant(): number {
    return this.paiementResult?.montantRestant || 0;
  }

  goToReservations(): void {
    this.router.navigate(['/trendy-places/mes-reservations']);
  }
}