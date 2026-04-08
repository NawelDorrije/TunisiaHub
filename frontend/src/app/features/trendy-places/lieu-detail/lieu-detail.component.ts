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

  // Pour la demo : userId hardcodé (à remplacer par ton auth service)
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
    this.showReservationModal = true;
  }

  closeReservation(): void {
    this.showReservationModal = false;
    this.selectedActivite = null;
  }

  confirmerReservation(): void {
    if (!this.selectedActivite || this.nombrePersonnes < 1) return;

    this.reservationLoading = true;
    this.reservationError = '';

    this.trendyService.creerReservation(
      this.selectedActivite.id!,
      this.currentUserId,
      { nombrePersonnes: this.nombrePersonnes }
    ).subscribe({
      next: () => {
        this.reservationLoading = false;
        this.reservationSuccess = '✅ Réservation effectuée avec succès !';
        setTimeout(() => this.closeReservation(), 2000);
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

  decrementerPersonnes() {
  if (this.nombrePersonnes > 1) {
    this.nombrePersonnes--;
  }
}

incrementerPersonnes() {
  const max = this.selectedActivite?.capaciteMax || 10;
  if (this.nombrePersonnes < max) {
    this.nombrePersonnes++;
  }
}
}