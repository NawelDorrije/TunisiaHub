import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../../services/api.service';

@Component({
  selector: 'app-admin-restaurant-reservations',
  templateUrl: './admin-restaurant-reservations.component.html',
  styleUrls: ['./admin-restaurant-reservations.component.css'],
})
export class AdminRestaurantReservationsComponent implements OnInit {
  reservations: any[] = [];
  loading = true;
  loadError: string | null = null;

  showAssignModal = false;
  assignReservation: any = null;
  availableTables: any[] = [];
  selectedTableIds: number[] = [];
  loadingTables = false;
  submittingConfirm = false;

  constructor(private api: ApiService) {}

  ngOnInit(): void {
    this.loadReservations();
  }

  get restaurantReservations(): any[] {
    return this.reservations.filter((r) => r.type === 'RestaurantReservation');
  }

  loadReservations(): void {
    this.loading = true;
    this.loadError = null;
    this.api.getAllReservations().subscribe({
      next: (data) => {
        this.reservations = Array.isArray(data) ? data : [];
        this.loading = false;
      },
      error: (err) => {
        console.error(err);
        this.loadError = 'Could not load reservations.';
        this.loading = false;
      },
    });
  }

  formatDateTime(val: unknown): string {
    if (val == null) return '—';
    if (typeof val === 'string') return val.replace('T', ' ').slice(0, 16);
    if (Array.isArray(val) && val.length >= 5) {
      const y = val[0];
      const m = val[1];
      const d = val[2];
      const h = val[3] ?? 0;
      const min = val[4] ?? 0;
      const pad = (n: number) => String(n).padStart(2, '0');
      return `${y}-${pad(Number(m))}-${pad(Number(d))} ${pad(Number(h))}:${pad(Number(min))}`;
    }
    return String(val);
  }

  restaurantName(r: any): string {
    return r?.restaurant?.name ?? '—';
  }

  userLabel(r: any): string {
    const u = r?.user;
    if (!u) return '—';
    return (u.email ?? `${u.prenom ?? ''} ${u.nom ?? ''}`.trim()) || '—';
  }

  tablesLabel(r: any): string {
    const tables = r?.tables;
    if (!tables?.length) return '—';
    return tables
      .map((t: any) => `#${t.tableNumber ?? t.id} (${t.capacity ?? '?'} seats)`)
      .join(', ');
  }

  isPending(r: any): boolean {
    return r?.status === 'PENDING';
  }

  openAssignModal(r: any): void {
    const rid = r?.restaurant?.id;
    if (rid == null) {
      alert('Reservation has no restaurant.');
      return;
    }
    this.assignReservation = r;
    this.selectedTableIds = [];
    this.availableTables = [];
    this.showAssignModal = true;
    this.loadingTables = true;
    this.api.getTablesByRestaurant(rid, 'AVAILABLE').subscribe({
      next: (tables) => {
        this.availableTables = Array.isArray(tables) ? tables : [];
        this.loadingTables = false;
      },
      error: (err) => {
        console.error(err);
        this.loadingTables = false;
        alert('Could not load tables for this restaurant.');
      },
    });
  }

  closeAssignModal(): void {
    this.showAssignModal = false;
    this.assignReservation = null;
    this.availableTables = [];
    this.selectedTableIds = [];
    this.submittingConfirm = false;
  }

  toggleTable(id: number, checked: boolean): void {
    if (checked) {
      if (!this.selectedTableIds.includes(id)) this.selectedTableIds.push(id);
    } else {
      this.selectedTableIds = this.selectedTableIds.filter((x) => x !== id);
    }
  }

  isTableSelected(id: number): boolean {
    return this.selectedTableIds.includes(id);
  }

  get selectedCapacity(): number {
    return this.availableTables
      .filter((t) => this.selectedTableIds.includes(t.id))
      .reduce((s, t) => s + (Number(t.capacity) || 0), 0);
  }

  get partySize(): number | null {
    const ps = this.assignReservation?.partySize;
    if (ps == null) return null;
    const n = Number(ps);
    return Number.isFinite(n) ? n : null;
  }

  submitConfirm(): void {
    if (!this.assignReservation?.id) return;
    if (this.selectedTableIds.length === 0) {
      alert('Select at least one table.');
      return;
    }
    const ps = this.partySize;
    if (ps != null && this.selectedCapacity < ps) {
      alert(
        `Selected tables seat ${this.selectedCapacity} guests; party size is ${ps}. Add more tables.`,
      );
      return;
    }
    this.submittingConfirm = true;
    this.api.confirmReservation(this.assignReservation.id, this.selectedTableIds).subscribe({
      next: () => {
        this.closeAssignModal();
        this.loadReservations();
        alert('Reservation confirmed and tables assigned.');
      },
      error: (err: any) => {
        console.error(err);
        this.submittingConfirm = false;
        const msg =
          err?.error?.message ??
          (typeof err?.error === 'string' ? err.error : null) ??
          'Could not confirm. Tables must be AVAILABLE and belong to this restaurant.';
        alert(msg);
      },
    });
  }

  refuseReservation(r: any): void {
    if (!r?.id) return;
    if (!confirm('Refuse (cancel) this reservation?')) return;
    this.api.cancelReservationById(r.id).subscribe({
      next: () => {
        this.loadReservations();
        alert('Reservation refused (cancelled).');
      },
      error: (err) => {
        console.error(err);
        alert('Could not cancel reservation.');
      },
    });
  }
}
