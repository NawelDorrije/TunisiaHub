import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { TrendyPlacesService } from '../../../services/trendy-places.service';
import * as QRCode from 'qrcode';

@Component({
  selector: 'app-facture',
  templateUrl: './facture.component.html',
  styleUrls: ['./facture.component.css']
})
export class FactureComponent implements OnInit {
  reservation: any = null;
  token: string = '';
  qrUrl: string = '';
  loading = true;

  ngrokUrl = 'https://upheld-racing-saddling.ngrok-free.dev';

  constructor(
    private route: ActivatedRoute,
    private trendyService: TrendyPlacesService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));

    this.trendyService.getMesReservations(1).subscribe({
      next: (data) => {
        this.reservation = data.find((r: any) => r.id === id);
        if (this.reservation) {
          this.genererFacture();
        } else {
          this.loading = false;
        }
      }
    });
  }

  genererFacture(): void {
    this.trendyService.genererFacture(this.reservation.id).subscribe({
      next: async (res) => {
        this.token = res.token;
        const verifyUrl = `${this.ngrokUrl}/api/factures/verify/${this.token}`;
        
        
        // ✅ Généré localement — pas d'API externe
        this.qrUrl = await QRCode.toDataURL(verifyUrl, {
          width: 200,
          margin: 1,
          color: {
            dark: '#0f3460',   // couleur des modules (bleu marine)
            light: '#ffffff'   // fond blanc
          }
        });

        this.loading = false;
      },
      error: (err) => {
        console.error('Erreur:', err);
        this.loading = false;
      }
    });
  }

  getDateFormatee(): string {
    if (!this.reservation?.dateReservation) return '';
    return new Date(this.reservation.dateReservation)
      .toLocaleDateString('fr-FR', { day: '2-digit', month: 'long', year: 'numeric' });
  }

  getDateEvenement(): string {
    const d = this.reservation?.activite?.dateEvenement;
    if (!d) return 'Non définie';
    return new Date(d).toLocaleDateString('fr-FR', {
      weekday: 'long', day: '2-digit', month: 'long', year: 'numeric'
    });
  }

  getNumeroFacture(): string {
    return 'TH-' + String(this.reservation?.id).padStart(6, '0');
  }

  imprimer(): void {
    window.print();
  }
}