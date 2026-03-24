import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
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
export class CampingDetailsComponent implements OnInit {
  camping: Camping = { id:0, name:'', location:'', campingType:'', price:0, description:'', startDate:'', endDate:'', photos:[], spots:[] };
  loading = true;
  selectedSpot?: Spot;

  // Variable pour contrôler le popup
  showReservationPopup = false;

  constructor(
    private campingService: CampingService,
    private reservationService: ReservationCampingService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.params['id']);
    this.loadCampingDetails(id);
  }

  loadCampingDetails(id: number) {
    this.campingService.getCampingById(id).subscribe({
      next: (data) => { this.camping = data; this.loading = false; },
      error: (err) => { console.error(err); this.loading = false; }
    });
  }

  // Ouvrir le "popup"
  openReservationPopup(spot: Spot) {
    this.selectedSpot = spot;
    this.showReservationPopup = true;
  }

  // Fermer le "popup"
  closeReservationPopup() {
    this.showReservationPopup = false;
  }

  confirmReservation() {
    if (!this.selectedSpot?.id) return;

    const reservation: Reservation = {
      startDateCamping: this.camping.startDate,
      endDateCamping: this.camping.endDate,
      numberOfPeopleCamping: 1,
      totalPriceCamping: this.selectedSpot.price,
      statusCamping: 'PENDING',
      userId: 1,
      spotId: this.selectedSpot.id
    };

    this.reservationService.createReservation(reservation).subscribe({
      next: () => {
        alert('Réservation confirmée ✅');
        this.closeReservationPopup();
      },
      error: (err) => {
        console.error(err);
        alert('Erreur lors de la réservation ❌');
      }
    });
  }
}
