import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ReservationService } from '../../../accommodations/services/reservation.service';
import { ReservationResponse } from '../../../../models/accommodations/reservation.model';

@Component({
  selector: 'app-my-accommodation-reservations',
  templateUrl: './my-accommodation-reservations.component.html',
  styleUrls: ['./my-accommodation-reservations.component.css']
})
export class MyAccommodationReservationsComponent implements OnInit {

  reservations: ReservationResponse[] = [];
  isLoading = true;
  errorMessage = '';
  successMessage = '';

  // Edit mode
  editingId: number | null = null;
  editStartDate: string = '';
  editEndDate: string = '';
  editMinEndDate: string = '';
  editError: string = '';
  today: string = new Date().toISOString().split('T')[0];

  constructor(
    private reservationService: ReservationService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadReservations();
  }

  loadReservations(): void {
    this.isLoading = true;
    this.reservationService.getMyReservations().subscribe({
      next: (data) => {
        this.reservations = data;
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'Failed to load reservations.';
        this.isLoading = false;
      }
    });
  }

  getNights(startDate: string, endDate: string): number {
    const diff = new Date(endDate).getTime() - new Date(startDate).getTime();
    return Math.round(diff / (1000 * 60 * 60 * 24));
  }

  cancelReservation(id: number): void {
    if (confirm('Are you sure you want to cancel this reservation?')) {
      this.reservationService.cancelReservation(id).subscribe({
        next: () => {
          this.successMessage = 'Reservation cancelled successfully.';
          this.errorMessage = '';
          const index = this.reservations.findIndex(r => r.id === id);
          if (index !== -1) this.reservations[index].status = 'CANCELLED';
        },
        error: () => {
          this.errorMessage = 'Failed to cancel reservation.';
        }
      });
    }
  }

  startEdit(reservation: ReservationResponse): void {
    this.editingId = reservation.id;
    this.editStartDate = reservation.startDate;
    this.editEndDate = reservation.endDate;
    this.editError = '';
    this.updateMinEndDate();
  }

  updateMinEndDate(): void {
    if (this.editStartDate) {
      const next = new Date(this.editStartDate);
      next.setDate(next.getDate() + 1);
      this.editMinEndDate = next.toISOString().split('T')[0];
    }
  }

  saveEdit(): void {
    if (!this.editStartDate || !this.editEndDate) {
      this.editError = 'Please select both dates.';
      return;
    }

    this.reservationService.editReservation(this.editingId!, {
      startDate: this.editStartDate,
      endDate: this.editEndDate
    }).subscribe({
      next: (updated) => {
        this.successMessage = 'Reservation updated successfully.';
        this.errorMessage = '';
        this.editError = '';
        const index = this.reservations.findIndex(r => r.id === updated.id);
        if (index !== -1) this.reservations[index] = updated;
        this.editingId = null;
      },
      error: () => {
        this.editError = 'These dates are not available. Please choose different dates.';
      }
    });
  }

  cancelEdit(): void {
    this.editingId = null;
    this.editError = '';
  }

  goBack(): void {
    this.router.navigate(['/my-reservations']);
  }
}