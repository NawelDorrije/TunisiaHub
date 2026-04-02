import { Component, OnInit } from '@angular/core';
import { ReservationCampingService } from '../../../../services/shared-reservation/reservation-camping.service';

@Component({
  selector: 'app-reservation',
  templateUrl: './reservation.component.html',
  styleUrls: ['./reservation.component.css']
})
export class ReservationComponent implements OnInit {

  reservations: any[] = [];

  loading = true;

  BASE_URL = 'http://localhost:8089';

  constructor(
    private reservationService: ReservationCampingService
  ) {}

  ngOnInit(): void {
    this.loadReservations();
  }

  loadReservations() {
    this.reservationService.getAllReservations().subscribe({
      next: (data) => {
        console.log("Reservations:", data);
        this.reservations = data;
        this.loading = false;
      },
      error: (err) => {
        console.error(err);
        this.loading = false;
        alert("Erreur lors du chargement des réservations");
      }
    });
  }

  getImageUrl(photo: string): string {

    if (!photo) {
      return 'assets/default-camping.jpg';
    }

    return `${this.BASE_URL}/${photo}`;
  }

  acceptReservation(reservation: any) {

    reservation.statusCamping = 'CONFIRMED';

    this.reservationService.updateReservation(reservation).subscribe({
      next: () => {
        alert("Réservation acceptée");
        this.loadReservations();
      },
      error: () => {
        alert("Erreur lors de l'acceptation");
      }
    });

  }

  cancelReservation(reservation: any) {

    reservation.statusCamping = 'CANCELLED';

    this.reservationService.updateReservation(reservation).subscribe({
      next: () => {
        alert("Réservation annulée");
        this.loadReservations();
      },
      error: () => {
        alert("Erreur lors de l'annulation");
      }
    });

  }

}
