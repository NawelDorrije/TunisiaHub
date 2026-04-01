import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CampingService } from '../../../services/campings/camping.service';
import { ReservationCampingService } from '../../../services/shared-reservation/reservation-camping.service';
import { Camping } from '../../../models/campings/camping';
import { Spot } from '../../../models/campings/spot';
import { Reservation } from '../../../models/shared-reservation/reservation';

@Component({
  selector: 'app-camping-details',
  templateUrl: './camping-details.component.html',
  styleUrls: ['./camping-details.component.css']
})
export class CampingDetailsComponent implements OnInit, OnDestroy {

  camping: Camping = {
    id: 0,
    name: '',
    location: '',
    campingType: '',
    price: 0,
    description: '',
    startDate: '',
    endDate: '',
    photos: [],
    spots: []
  };

  loading = true;
  selectedSpot?: Spot;

  currentImageIndex = 0;
  slideshowInterval: any;
  BASE_URL = 'http://localhost:8089';

  showReservationPopup = false;

  constructor(
    private campingService: CampingService,
    private reservationService: ReservationCampingService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (id) {
      this.loadCampingDetails(id);
    }
  }

  ngOnDestroy(): void {
    if (this.slideshowInterval) {
      clearInterval(this.slideshowInterval);
    }
  }

  loadCampingDetails(id: number): void {
    this.loading = true;
    this.campingService.getCampingById(id).subscribe({
      next: (data) => {
        this.camping = data;
        this.loading = false;
        if (this.camping.photos && this.camping.photos.length > 1) {
          this.startSlideshow();
        }
      },
      error: (err) => {
        console.error('Erreur lors du chargement des détails:', err);
        this.loading = false;
        alert('Failed to load camping details');
      }
    });
  }

  // ── GALLERY ──────────────────────────────────────────────

  startSlideshow(): void {
    this.slideshowInterval = setInterval(() => {
      this.nextImage();
    }, 4000);
  }

  getCurrentPhoto(camping: Camping): string {
    if (camping.photos && camping.photos.length > 0) {
      return `${this.BASE_URL}/${camping.photos[this.currentImageIndex]}`;
    }
    return 'assets/default-camping.jpg';
  }

  nextImage(): void {
    if (!this.camping.photos || this.camping.photos.length === 0) return;
    this.currentImageIndex = (this.currentImageIndex + 1) % this.camping.photos.length;
  }

  prevImage(): void {
    if (!this.camping.photos || this.camping.photos.length === 0) return;
    this.currentImageIndex =
      (this.currentImageIndex - 1 + this.camping.photos.length) % this.camping.photos.length;
  }

  // ── RESERVATION ──────────────────────────────────────────

  openReservationPopup(spot: Spot): void {
    this.selectedSpot = spot;
    this.showReservationPopup = true;
  }

  closeReservationPopup(): void {
    this.showReservationPopup = false;
    this.selectedSpot = undefined;
  }

  /** Ferme la modal si le clic est sur l'overlay (fond sombre) */
  onOverlayClick(event: MouseEvent): void {
    if ((event.target as HTMLElement).classList.contains('modal-overlay')) {
      this.closeReservationPopup();
    }
  }

  confirmReservation(): void {
    if (!this.selectedSpot?.id) {
      alert('No spot selected');
      return;
    }

    const reservation: Reservation = {
      startDateCamping: this.camping.startDate,
      endDateCamping: this.camping.endDate,
      numberOfPeopleCamping: 1,
      totalPriceCamping: this.selectedSpot.price,
      statusCamping: 'PENDING',
      userId: 1,           // À remplacer par l'ID de l'utilisateur connecté
      spotId: this.selectedSpot.id
    };

    this.reservationService.createReservation(reservation).subscribe({
      next: () => {
        alert('✅ Reservation confirmed successfully!');
        this.closeReservationPopup();
      },
      error: (err) => {
        console.error(err);
        alert('❌ Error while creating reservation. Please try again.');
      }
    });
  }

  // ── NAVIGATION ───────────────────────────────────────────

  goBack(): void {
    this.router.navigate(['/campings']);
  }
}
