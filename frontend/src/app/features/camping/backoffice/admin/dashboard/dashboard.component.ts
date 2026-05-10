import { Component, OnInit } from '@angular/core';
import { CampingService } from '../../../../../services/campings/camping.service';
import { ReservationService } from '../../../../../services/shared-reservation/reservation-camping.service';
import { PaymentService } from '../../../../../services/shared-reservation/payment.service';
import { forkJoin } from 'rxjs';
@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent  implements OnInit  {
  today: Date = new Date();
 stats = { totalCampings: 0, activeCampings: 0, pendingCampings: 0,
            totalReservations: 0, totalRevenue: 0, pendingReservations: 0 };
  recentCampings: any[] = [];
  recentReservations: any[] = [];
  loading = true;

  constructor(
    private campingService: CampingService,
    private reservationService: ReservationService,
    private paymentService: PaymentService


  ) {}

  ngOnInit(): void {
    forkJoin({
      campings: this.campingService.getAllCampings(),
      reservations: this.reservationService.getAllReservations(),
      payments: this.paymentService.getAllPayments()
    }).subscribe({
      next: ({ campings, reservations, payments }) => {
        this.stats.totalCampings = campings.length;
        this.stats.activeCampings = campings.filter(c => c.status === 'ACTIVE').length;
        this.stats.pendingCampings = campings.filter(c => c.status === 'PENDING').length;
        this.stats.totalReservations = reservations.length;
        this.stats.pendingReservations = reservations.filter(r => r.status === 'PENDING').length;
        this.stats.totalRevenue = payments
          .filter(p => p.status === 'SUCCESS')
          .reduce((sum, p) => sum + (p.amount || 0), 0);
        this.recentCampings = campings.slice(0, 5);
        this.recentReservations = reservations.slice(0, 5);
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  approveCamping(id: number): void {
    // implement approval logic
  }
}
