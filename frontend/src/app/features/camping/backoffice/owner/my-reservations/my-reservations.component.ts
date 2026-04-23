import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { HttpClient, HttpParams } from '@angular/common/http';
import { AuthService } from '../../../../auth/services/auth.service';

export interface OwnerReservation {
  id: number;
  clientName: string;
  clientEmail?: string;
  campingName: string;
  spotName: string;
  checkIn: string;
  checkOut: string;
  numberOfGuests: number;
  totalPrice: number;
  status: string;
  notes?: string;
  createdAt?: string;
  activityNames?: string[];
}

@Component({
  selector: 'app-my-reservations',
  templateUrl: './my-reservations.component.html',
  styleUrls: ['./my-reservations.component.css']
})
export class MyReservationsComponent implements OnInit, OnDestroy {

  private readonly API = 'http://localhost:8089/api/reservations';

  reservations: OwnerReservation[] = [];
  filtered: OwnerReservation[] = [];
  loading = true;
  error = '';
  successMsg = '';
  processingId: number | null = null;
  selectedReservation: OwnerReservation | null = null;

  searchQuery = '';
  statusFilter = 'ALL';
  dateFrom = '';
  dateTo = '';

  readonly statuses = ['ALL', 'PENDING', 'CONFIRMED', 'ACTIVE', 'COMPLETED', 'CANCELLED'];

  private destroy$ = new Subject<void>();

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadReservations();
  }
trackById(index: number, item: any): number {
  return item.id;
}
  loadReservations(): void {
    this.loading = true;
    this.error = '';
    this.http.get<OwnerReservation[]>(this.API)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: data => {
          this.reservations = data.sort((a, b) =>
            new Date(b.createdAt || '').getTime() - new Date(a.createdAt || '').getTime()
          );
          this.applyFilters();
          this.loading = false;
        },
        error: () => {
          this.error = 'Failed to load reservations. Please try again.';
          this.loading = false;
        }
      });
  }

  applyFilters(): void {
    let result = [...this.reservations];

    if (this.statusFilter !== 'ALL') {
      result = result.filter(r => r.status === this.statusFilter);
    }

    if (this.searchQuery.trim()) {
      const q = this.searchQuery.toLowerCase();
      result = result.filter(r =>
        r.clientName?.toLowerCase().includes(q) ||
        r.campingName?.toLowerCase().includes(q) ||
        r.spotName?.toLowerCase().includes(q) ||
        String(r.id).includes(q)
      );
    }

    if (this.dateFrom) {
      result = result.filter(r => new Date(r.checkIn) >= new Date(this.dateFrom));
    }
    if (this.dateTo) {
      result = result.filter(r => new Date(r.checkOut) <= new Date(this.dateTo));
    }

    this.filtered = result;
  }

  setStatusFilter(status: string): void {
    this.statusFilter = status;
    this.applyFilters();
  }

  onSearch(): void {
    this.applyFilters();
  }

  clearFilters(): void {
    this.searchQuery = '';
    this.statusFilter = 'ALL';
    this.dateFrom = '';
    this.dateTo = '';
    this.applyFilters();
  }

  confirmReservation(r: OwnerReservation): void {
    this.updateStatus(r.id, 'CONFIRMED');
  }

  cancelReservation(r: OwnerReservation): void {
    if (!confirm(`Cancel reservation #${r.id} for ${r.clientName}?`)) return;
    this.http.delete<void>(`${this.API}/${r.id}/cancel`)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.setLocalStatus(r.id, 'CANCELLED');
          this.showSuccess('Reservation cancelled.');
          if (this.selectedReservation?.id === r.id) this.selectedReservation.status = 'CANCELLED';
        },
        error: () => { this.error = 'Failed to cancel reservation.'; }
      });
  }

  private updateStatus(id: number, status: string): void {
    this.processingId = id;
    const params = new HttpParams().set('status', status);
    this.http.patch<OwnerReservation>(`${this.API}/${id}/status`, null, { params })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: updated => {
          this.setLocalStatus(id, updated.status);
          this.showSuccess(`Reservation #${id} marked as ${updated.status}.`);
          if (this.selectedReservation?.id === id) this.selectedReservation.status = updated.status;
          this.processingId = null;
        },
        error: () => {
          this.error = 'Failed to update reservation status.';
          this.processingId = null;
        }
      });
  }

  private setLocalStatus(id: number, status: string): void {
    const idx = this.reservations.findIndex(r => r.id === id);
    if (idx >= 0) this.reservations[idx].status = status;
    this.applyFilters();
  }

  openDetail(r: OwnerReservation): void {
    this.selectedReservation = r;
  }

  closeDetail(): void {
    this.selectedReservation = null;
  }

  private showSuccess(msg: string): void {
    this.successMsg = msg;
    setTimeout(() => this.successMsg = '', 4000);
  }

  getNights(checkIn: string, checkOut: string): number {
    return Math.max(1, Math.ceil(
      (new Date(checkOut).getTime() - new Date(checkIn).getTime()) / 86400000
    ));
  }

  getCount(status: string): number {
    return status === 'ALL'
      ? this.reservations.length
      : this.reservations.filter(r => r.status === status).length;
  }

  canConfirm(status: string): boolean { return status === 'PENDING'; }
  canCancel(status: string): boolean  { return ['PENDING', 'CONFIRMED'].includes(status); }
  isProcessing(id: number): boolean   { return this.processingId === id; }

  get stats() {
    return {
      total:     this.reservations.length,
      pending:   this.reservations.filter(r => r.status === 'PENDING').length,
      confirmed: this.reservations.filter(r => r.status === 'CONFIRMED').length,
      active:    this.reservations.filter(r => r.status === 'ACTIVE').length,
      completed: this.reservations.filter(r => r.status === 'COMPLETED').length,
      cancelled: this.reservations.filter(r => r.status === 'CANCELLED').length,
      revenue:   this.reservations
        .filter(r => !['CANCELLED'].includes(r.status))
        .reduce((s, r) => s + (r.totalPrice || 0), 0),
    };
  }

  hasActiveFilters(): boolean {
    return this.statusFilter !== 'ALL' || !!this.searchQuery || !!this.dateFrom || !!this.dateTo;
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
