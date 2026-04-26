import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../../../services/api.service';
import { AuthService } from '../../../auth/services/auth.service';

@Component({
  selector: 'app-my-reservations',
  templateUrl: './my-reservations.component.html',
  styleUrls: ['./my-reservations.component.css']
})
export class MyReservationsComponent implements OnInit {
  reservations: any[] = [];
  loading = true;
  selectedStatus: string = 'ALL';

  constructor(
    private api: ApiService,
    private auth: AuthService
  ) { }

  ngOnInit(): void {
    this.loadReservations();
  }

  get filteredReservations(): any[] {
    if (this.selectedStatus === 'ALL') {
      return this.reservations;
    }
    return this.reservations.filter((r) => r.status === this.selectedStatus);
  }

  loadReservations(): void {
    const userId = this.auth.getUserId();
    console.log('DEBUG: Current User ID from AuthService:', userId);
    
    if (!userId) {
      console.warn('No user ID found in AuthService, please re-login.');
      this.loading = false;
      return;
    }

    this.loading = true;
    this.api.getReservationsByUser(userId).subscribe({
      next: (data) => {
        console.log('DEBUG: Reservations received for user', userId, ':', data);
        this.reservations = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('DEBUG: Error loading reservations for user', userId, ':', err);
        this.loading = false;
      }
    });
  }

  formatDateTime(val: any): string {
    if (!val) return '—';
    if (Array.isArray(val) && val.length >= 5) {
      const y = val[0];
      const m = val[1];
      const d = val[2];
      const h = val[3] ?? 0;
      const min = val[4] ?? 0;
      const pad = (n: number) => String(n).padStart(2, '0');
      return `${y}-${pad(Number(m))}-${pad(Number(d))} ${pad(Number(h))}:${pad(Number(min))}`;
    }
    if (typeof val === 'string') {
        return val.replace('T', ' ').slice(0, 16);
    }
    return String(val);
  }

  cancelReservation(id: number): void {
    if (!confirm('Are you sure you want to cancel this reservation?')) return;
    
    this.api.cancelReservationById(id).subscribe({
      next: () => {
        this.loadReservations();
      },
      error: (err) => {
        console.error('Error cancelling reservation', err);
        alert('Could not cancel reservation. Please try again.');
      }
    });
  }
}
