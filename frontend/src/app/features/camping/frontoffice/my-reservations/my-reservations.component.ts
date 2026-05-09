import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { Router } from '@angular/router';
import { Reservation } from '../../../../models/shared-reservation/reservation';
import { ReservationService } from '../../../../services/shared-reservation/reservation-camping.service';
import { AuthService } from '../../../auth/services/auth.service';

@Component({
  selector: 'app-my-reservations',
  templateUrl: './my-reservations.component.html',
  styleUrls: ['./my-reservations.component.css']
})
export class MyReservationsComponent implements OnInit, OnDestroy {
  reservations: Reservation[] = [];
  filteredReservations: Reservation[] = [];
  loading = true;
  error = '';
  successMsg = '';
  cancellingId: number | null = null;
  selectedReservation: Reservation | null = null;
  statusFilter = 'ALL';
  userId!: number;
  private destroy$ = new Subject<void>();

  statuses = ['ALL', 'PENDING', 'PAID', 'CONFIRMED', 'ACTIVE', 'COMPLETED', 'CANCELLED'];

  constructor(
    private reservationService: ReservationService,
    private router: Router,
        private authService: AuthService

  ) {}

ngOnInit(): void {
  const userId = this.authService.getUserId();

  if (!userId) {
    this.router.navigate(['/auth/sign-in']);
    return;
  }

  this.userId = userId;
  this.loadReservations();
}


  loadReservations(): void {
    this.loading = true;
    this.error = '';
    this.reservationService.getByUser(this.userId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data) => {
          // Sort: most recent first
          this.reservations = data.sort((a, b) =>
            new Date(b.createdAt || '').getTime() - new Date(a.createdAt || '').getTime()
          );
          this.applyFilter();
          this.loading = false;
        },
        error: () => {
          this.error = 'Could not load your reservations. Please try again.';
          this.loading = false;
        }
      });
  }

  applyFilter(): void {
    this.filteredReservations = this.statusFilter === 'ALL'
      ? [...this.reservations]
      : this.reservations.filter(r => r.status === this.statusFilter);
  }

  setFilter(status: string): void {
    this.statusFilter = status;
    this.applyFilter();
  }

  cancelReservation(reservation: Reservation): void {
    if (!confirm(`Cancel reservation for ${reservation.campingName || 'this spot'}?`)) return;
    this.cancellingId = reservation.id!;
    this.error = '';
    this.reservationService.cancelReservation(reservation.id!)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.successMsg = 'Reservation cancelled successfully.';
          this.cancellingId = null;
          // Update locally
          const idx = this.reservations.findIndex(r => r.id === reservation.id);
          if (idx >= 0) this.reservations[idx].status = 'CANCELLED';
          this.applyFilter();
          setTimeout(() => this.successMsg = '', 4000);
        },
        error: () => {
          this.error = 'Failed to cancel reservation. Please try again.';
          this.cancellingId = null;
        }
      });
  }

  viewDetail(reservation: Reservation): void {
    this.selectedReservation = reservation;
  }

  closeDetail(): void {
    this.selectedReservation = null;
  }

  goToCamping(reservation: Reservation): void {
    if (reservation.spotId) {
      // Navigate to the camping — you may need to adjust based on your data
      this.router.navigate(['/camping']);
    }
  }

  canCancel(status?: string): boolean {
    return status === 'PENDING' || status === 'PAID' || status === 'CONFIRMED';
  }

  getStatusClass(status?: string): string {
    const map: Record<string, string> = {
      PENDING: 'st-pending', PAID: 'st-paid', CONFIRMED: 'st-confirmed',
      ACTIVE: 'st-active', COMPLETED: 'st-completed', CANCELLED: 'st-cancelled'
    };
    return map[status || ''] || '';
  }

  getStatusIcon(status?: string): string {
    const map: Record<string, string> = {
      PENDING: '⏳', PAID: '💳', CONFIRMED: '✅',
      ACTIVE: '🏕', COMPLETED: '🎉', CANCELLED: '❌'
    };
    return map[status || ''] || '📋';
  }

  getNights(checkIn: string, checkOut: string): number {
    return Math.max(0, Math.ceil(
      (new Date(checkOut).getTime() - new Date(checkIn).getTime()) / 86400000
    ));
  }

  getFilterCount(status: string): number {
    return status === 'ALL'
      ? this.reservations.length
      : this.reservations.filter(r => r.status === status).length;
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
