import { Component, OnInit, ViewChild, ElementRef, Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { Modal } from 'bootstrap';
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

  @ViewChild('reservationModal') reservationModal!: ElementRef;
  private modalInstance!: Modal;

  isBrowser: boolean;

  constructor(
    private campingService: CampingService,
    private reservationService: ReservationCampingService,
    private route: ActivatedRoute,
    @Inject(PLATFORM_ID) platformId: Object
  ) {
    this.isBrowser = isPlatformBrowser(platformId); // ✅ permet de savoir si on est côté navigateur
  }

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

  openReservationModal(spot: Spot): void {
    if (!this.isBrowser) return; // on ne fait rien côté serveur
    this.selectedSpot = spot;
    if (!this.modalInstance) {
      this.modalInstance = new Modal(this.reservationModal.nativeElement);
    }
    this.modalInstance.show();
  }

  confirmReservation(): void {
    if (!this.selectedSpot) return;
 const reservation: Reservation = {
  startDateCamping: this.camping.startDate,
  endDateCamping: this.camping.endDate,
  numberOfPeopleCamping: 1,
  totalPriceCamping: this.selectedSpot.price,
  statusCamping: 'PENDING',
  user: { id: 1 },
  spot: { id: this.selectedSpot.id! }
};
    this.reservationService.createReservation(reservation).subscribe({
      next: () => { alert('Réservation confirmée ✅'); this.modalInstance?.hide(); },
      error: (err) => { console.error(err); alert('Erreur lors de la réservation ❌'); }
    });
  }
}
