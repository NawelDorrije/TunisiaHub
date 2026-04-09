import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { Camping } from '../../../../../models/campings/camping';
import { CampingService } from '../../../../../services/campings/camping.service';

@Component({
  selector: 'app-my-campings',
  templateUrl: './my-campings.component.html',
  styleUrls: ['./my-campings.component.css'],
})
export class MyCampingsComponent implements OnInit, OnDestroy {
  campings: Camping[] = [];
  loading = true;
  deleteTarget: Camping | null = null;
  deleting = false;
  successMsg: string | null = null;
  errorMsg: string | null = null;
  viewMode: 'grid' | 'list' = 'grid';

  /** Controls which camping's action-menu is open (by id) */
  openMenuId: number | null = null;

  private destroy$ = new Subject<void>();

  constructor(
    private campingService: CampingService,
    private router: Router,
  ) {}

  ngOnInit(): void { this.load(); }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  load(): void {
    this.loading = true;
    this.campingService.getByOwner(2) // TODO: replace with real owner id
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data) => { this.campings = data; this.loading = false; },
        error: () => { this.loading = false; this.errorMsg = 'Failed to load campings.'; },
      });
  }

  toggleMenu(id: number, event: MouseEvent): void {
    event.stopPropagation();
    this.openMenuId = this.openMenuId === id ? null : id;
  }

  closeMenu(): void { this.openMenuId = null; }

  confirmDelete(c: Camping, event?: MouseEvent): void {
    if (event) event.stopPropagation();
    this.closeMenu();
    this.deleteTarget = c;
  }

  cancelDelete(): void { this.deleteTarget = null; }

  executeDelete(): void {
    if (!this.deleteTarget) return;
    this.deleting = true;
    this.campingService.deleteCamping(this.deleteTarget.id!)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.campings = this.campings.filter(c => c.id !== this.deleteTarget!.id);
          this.deleteTarget = null;
          this.deleting = false;
          this.flash('success', 'Camping deleted successfully.');
        },
        error: () => {
          this.deleting = false;
          this.deleteTarget = null;
          this.flash('error', 'Failed to delete camping. Please try again.');
        },
      });
  }

  getStatusClass(status: string): string {
    const map: Record<string, string> = {
      ACTIVE: 'st--active',
      PENDING: 'st--pending',
      SUSPENDED: 'st--suspended',
      CLOSED: 'st--closed',
    };
    return map[status] ?? '';
  }

  get totalActive(): number  { return this.campings.filter(c => c.status === 'ACTIVE').length; }
  get totalPending(): number { return this.campings.filter(c => c.status === 'PENDING').length; }

  navigateTo(path: string[]): void {
    this.closeMenu();
    this.router.navigate(path);
  }

  private flash(type: 'success' | 'error', msg: string): void {
    if (type === 'success') { this.successMsg = msg; this.errorMsg = null; }
    else                    { this.errorMsg = msg; this.successMsg = null; }
    setTimeout(() => { this.successMsg = null; this.errorMsg = null; }, 4500);
  }
}
