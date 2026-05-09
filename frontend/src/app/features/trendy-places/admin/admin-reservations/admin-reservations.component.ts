import { Component, OnInit } from '@angular/core';
import { TrendyPlacesService } from '../../../../services/trendy-places.service';

@Component({
  selector: 'app-admin-reservations',
  templateUrl: './admin-reservations.component.html',
  styleUrls: ['./admin-reservations.component.css']
})
export class AdminReservationsComponent implements OnInit {
  reservations: any[] = [];
  filteredReservations: any[] = [];
  loading = true;
  successMessage = '';
  errorMessage = '';
  updatingId: number | null = null;
  filterStatut = 'TOUS';

  statuts = ['TOUS', 'EN_ATTENTE', 'PAYEE', 'CONFIRMEE', 'ANNULEE'];

  constructor(private service: TrendyPlacesService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.service.getAllReservationsAdmin().subscribe({
      next: (data) => {
        this.reservations = data.sort((a: any, b: any) =>
          new Date(b.dateReservation).getTime() - new Date(a.dateReservation).getTime()
        );
        this.applyFilter();
        this.loading = false;
      },
      error: () => {
        this.errorMessage = 'Erreur de chargement';
        this.loading = false;
      }
    });
  }

  applyFilter(): void {
    if (this.filterStatut === 'TOUS') {
      this.filteredReservations = [...this.reservations];
    } else {
      this.filteredReservations = this.reservations.filter(
        r => r.statut === this.filterStatut
      );
    }
  }

  setFilter(statut: string): void {
    this.filterStatut = statut;
    this.applyFilter();
  }

  changerStatut(id: number, statut: string): void {
    this.updatingId = id;
    this.successMessage = '';
    this.errorMessage = '';

    this.service.updateStatutReservation(id, statut).subscribe({
      next: () => {
        this.updatingId = null;
        const label = this.getStatutLabel(statut);
        this.successMessage = `Réservation #${id} mise à jour → ${label}`;
        this.load();
        setTimeout(() => this.successMessage = '', 4000);
      },
      error: () => {
        this.updatingId = null;
        this.errorMessage = 'Erreur lors de la mise à jour';
      }
    });
  }

  getStatutLabel(statut: string): string {
    const map: { [k: string]: string } = {
      'EN_ATTENTE': '⏳ En attente',
      'PAYEE': '💳 Payée',
      'CONFIRMEE': '✅ Confirmée',
      'ANNULEE': '❌ Annulée'
    };
    return map[statut] || statut;
  }

  getStatutClass(statut: string): string {
    const map: { [k: string]: string } = {
      'EN_ATTENTE': 'statut-attente',
      'PAYEE': 'statut-payee',
      'CONFIRMEE': 'statut-confirmee',
      'ANNULEE': 'statut-annulee'
    };
    return map[statut] || '';
  }

  getCount(statut: string): number {
    if (statut === 'TOUS') return this.reservations.length;
    return this.reservations.filter(r => r.statut === statut).length;
  }

  getRevenuTotal(): number {
    return this.reservations
      .filter(r => r.statut === 'CONFIRMEE' || r.statut === 'PAYEE')
      .reduce((sum, r) => sum + (r.prixTotal || 0), 0);
  }
}