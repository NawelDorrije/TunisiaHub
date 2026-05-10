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

  // Notification
  showNotifModal = false;
  selectedReservationId: number | null = null;
  notifActive = true;
  notifJours: 1 | 2 | 3 = 1;
  notifLoading = false;
  notifSuccess = '';

  currentUserId = 1;

  constructor(
    private trendyService: TrendyPlacesService,
    private router: Router
  ) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading = true;
    this.trendyService.getMesReservations(this.currentUserId).subscribe({
      next: (data) => { this.reservations = data; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  // ===== NOTIFICATION =====
  ouvrirNotif(reservation: any): void {
    this.selectedReservationId = reservation.id;
    this.notifActive = reservation.notificationActive ?? true;
    this.notifJours = reservation.notificationJoursAvant ?? 1;
    this.notifSuccess = '';
    this.showNotifModal = true;
  }

  fermerNotif(): void {
    this.showNotifModal = false;
    this.selectedReservationId = null;
  }

  sauvegarderNotif(): void {
    if (!this.selectedReservationId) return;
    this.notifLoading = true;

    this.trendyService.configurerNotification(
      this.selectedReservationId,
      this.notifActive,
      this.notifJours
    ).subscribe({
      next: () => {
        this.notifLoading = false;
        this.notifSuccess = 'Notification configurée !';
        this.load();
        setTimeout(() => {
          this.fermerNotif();
          this.notifSuccess = '';
        }, 1500);
      },
      error: () => {
        this.notifLoading = false;
      }
    });
  }

  // ===== PAIEMENT =====
  allerPayer(id: number): void {
    this.router.navigate(['/trendy-places/paiement', id]);
  }

  allerPayerTranche(id: number): void {
    this.router.navigate(['/trendy-places/paiement-tranche', id]);
  }

  annuler(id: number): void { this.cancellingId = id; }

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
      'PAYEE': 'statut-payee',
      'CONFIRMEE': 'statut-confirmee',
      'ANNULEE': 'statut-annulee'
    };
    return map[statut] || '';
  }

  getStatutLabel(statut: string): string {
    const map: {[k: string]: string} = {
      'EN_ATTENTE': '⏳ En attente de paiement',
      'PAYEE': '💳 Payée — en attente confirmation',
      'CONFIRMEE': '✅ Confirmée',
      'ANNULEE': '❌ Annulée'
    };
    return map[statut] || statut;
  }

  hasDateEvenement(r: any): boolean {
    return !!r.activite?.dateEvenement;
  }

  getDateEvenement(r: any): string {
    if (!r.activite?.dateEvenement) return '';
    const d = new Date(r.activite.dateEvenement);
    return d.toLocaleDateString('fr-FR', { day: '2-digit', month: '2-digit', year: 'numeric' });
  }

  getNotifIcon(r: any): string {
    if (!r.notificationActive) return '🔕';
    return '🔔';
  }
}