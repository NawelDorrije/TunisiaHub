import { Component, OnInit } from '@angular/core';
import { TrendyPlacesService } from '../../../services/trendy-places.service';
import { Router } from '@angular/router';  

@Component({
  selector: 'app-mes-reservations',
  templateUrl: './mes-reservations.component.html',
  styleUrls: ['./mes-reservations.component.css']
})
export class MesReservationsComponent implements OnInit {
  reservations: any[] = [];
  loading = true;
  cancellingId: number | null = null;
  successMessage = '';

  // Pour la demo : à remplacer par ton auth
  currentUserId = 1;

  constructor(
  private trendyService: TrendyPlacesService,
  private router: Router  // ← AJOUTE Router dans le constructeur
) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.trendyService.getMesReservations(this.currentUserId).subscribe({
      next: (data) => { this.reservations = data; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  annuler(id: number): void {
    this.cancellingId = id;
  }

  confirmerAnnulation(id: number): void {
    this.trendyService.annulerReservation(id).subscribe({
      next: () => {
        this.successMessage = 'Réservation annulée.';
        this.cancellingId = null;
        this.load();
        setTimeout(() => this.successMessage = '', 3000);
      }
    });
  }

  getStatutClass(statut: string): string {
  const map: {[k: string]: string} = {
    'EN_ATTENTE': 'statut-attente',
    'PAYEE': 'statut-payee',        // ← nouveau
    'CONFIRMEE': 'statut-confirmee',
    'ANNULEE': 'statut-annulee'
  };
  return map[statut] || '';
}

getStatutLabel(statut: string): string {
  const map: {[k: string]: string} = {
    'EN_ATTENTE': '⏳ En attente de paiement',
    'PAYEE': '💳 Payée — en attente confirmation',  // ← nouveau
    'CONFIRMEE': '✅ Confirmée',
    'ANNULEE': '❌ Annulée'
  };
  return map[statut] || statut;
}


  allerPayer(id: number): void {
  this.router.navigate(['/trendy-places/paiement', id]);
}
}