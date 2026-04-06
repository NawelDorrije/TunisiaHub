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
  loading  = true;
  error: string | null = null;
  successMsg: string | null = null;

  // Confirm dialog state
  showConfirm  = false;
  confirmTarget: Camping | null = null;
  deleting = false;

  // View toggle
  viewMode: 'grid' | 'table' = 'grid';

  private destroy$ = new Subject<void>();

  constructor(
    private campingService: CampingService,
    private router: Router,
  ) {}

  ngOnInit(): void { this.load(); }
  ngOnDestroy(): void { this.destroy$.next(); this.destroy$.complete(); }

  load(): void {
    this.loading = true;
    this.error = null;
    this.campingService.getAllCampings()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: data => { this.campings = data; this.loading = false; },
        error: ()  => { this.error = 'Failed to load campings.'; this.loading = false; },
      });
  }

  navigate(path: string[]): void { this.router.navigate(path); }

  requestDelete(camping: Camping): void {
    this.confirmTarget = camping;
    this.showConfirm = true;
  }

  cancelDelete(): void {
    this.showConfirm = false;
    this.confirmTarget = null;
  }

  confirmDelete(): void {
    if (!this.confirmTarget?.id) return;
    this.deleting = true;
    this.campingService.deleteCamping(this.confirmTarget.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.campings = this.campings.filter(c => c.id !== this.confirmTarget!.id);
          this.successMsg = `"${this.confirmTarget!.name}" deleted successfully.`;
          this.deleting = false;
          this.showConfirm = false;
          this.confirmTarget = null;
          setTimeout(() => this.successMsg = null, 4000);
        },
        error: () => {
          this.error = 'Delete failed. Please try again.';
          this.deleting = false;
          this.showConfirm = false;
        },
      });
  }

  getStatusClass(status: string): string {
    const map: Record<string, string> = {
      ACTIVE: 'badge--active', PENDING: 'badge--pending',
      SUSPENDED: 'badge--suspended', CLOSED: 'badge--closed',
    };
    return map[status] ?? '';
  }
}
