import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Lieu, ActiviteLieu } from '../../../models/trendy-places/lieu.model';
import { TrendyPlacesService } from '../../../services/trendy-places.service';

@Component({
  selector: 'app-lieu-detail',
  templateUrl: './lieu-detail.component.html',
  styleUrls: ['./lieu-detail.component.css']
})
export class LieuDetailComponent implements OnInit {
  lieu: Lieu | null = null;
  activites: ActiviteLieu[] = [];
  loading = true;

  // Réservation
  showReservationModal = false;
  selectedActivite: ActiviteLieu | null = null;
  nombrePersonnes = 1;
  reservationLoading = false;
  reservationSuccess = '';
  reservationError = '';

  // ← NOUVEAU : conflit
  conflits: any[] = [];
  conflitAccepte = false;
  conflitLoading = false;

  currentUserId = 1;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private trendyService: TrendyPlacesService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.trendyService.getLieuById(id).subscribe({
      next: (data) => {
        this.lieu = data;
        this.activites = data.activites || [];
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  openReservation(activite: ActiviteLieu): void {
    this.selectedActivite = activite;
    this.nombrePersonnes = 1;
    this.reservationSuccess = '';
    this.reservationError = '';
    this.conflits = [];
    this.conflitAccepte = false;
    this.showReservationModal = true;

    // Vérifier les conflits si l'activité a une date
    if (activite.dateEvenement) {
      this.conflitLoading = true;
      this.trendyService.getConflits(this.currentUserId, activite.id).subscribe({
        next: (data) => {
          this.conflits = data;
          this.conflitLoading = false;
        },
        error: () => { this.conflitLoading = false; }
      });
    }
  }

  closeReservation(): void {
    this.showReservationModal = false;
    this.selectedActivite = null;
    this.conflits = [];
    this.conflitAccepte = false;
  }

  hasConflitNonAccepte(): boolean {
    return this.conflits.length > 0 && !this.conflitAccepte;
  }

  confirmerReservation(): void {
    if (!this.selectedActivite || this.nombrePersonnes < 1) return;
    if (this.hasConflitNonAccepte()) return;

    this.reservationLoading = true;
    this.reservationError = '';

    this.trendyService.creerReservation(
      this.selectedActivite.id!,
      this.currentUserId,
      { nombrePersonnes: this.nombrePersonnes }
    ).subscribe({
      next: (res) => {
        this.reservationLoading = false;
        this.reservationSuccess = '✅ Réservation effectuée avec succès !';
        setTimeout(() => {
          this.closeReservation();
          // Rediriger vers paiement
          this.router.navigate(['/trendy-places/paiement', res.id]);
        }, 1500);
      },
      error: () => {
        this.reservationLoading = false;
        this.reservationError = '❌ Erreur lors de la réservation. Réessayez.';
      }
    });
  }

  getPrixTotal(): number {
    if (!this.selectedActivite) return 0;
    return (this.selectedActivite.prix || 0) * this.nombrePersonnes;
  }

  getImageUrl(image: string): string {
    if (!image) return '/assets/images/lieux/default.jpg';
    if (image.startsWith('http')) return image;
    return `/assets/images/lieux/${image}`;
  }

  onImageError(event: Event): void {
    (event.target as HTMLImageElement).src = '/assets/images/lieux/default.jpg';
  }

  goBack(): void {
    this.router.navigate(['/trendy-places']);
  }

  decrementerPersonnes(): void {
    if (this.nombrePersonnes > 1) this.nombrePersonnes--;
  }

  incrementerPersonnes(): void {
    const max = this.selectedActivite?.capaciteMax || 10;
    if (this.nombrePersonnes < max) this.nombrePersonnes++;
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return '';
    const d = new Date(dateStr);
    return d.toLocaleDateString('fr-FR', { weekday: 'long', day: '2-digit', month: 'long', year: 'numeric' });
  }
}