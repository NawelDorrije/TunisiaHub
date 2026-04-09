import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { Camping } from '../../../../../models/campings/camping';
import { Spot } from '../../../../../models/campings/spot';
import { SpotService } from '../../../../../services/campings/spot.service';
import { CampingService } from '../../../../../services/campings/camping.service';

@Component({
  selector: 'app-spot-management',
  templateUrl: './spot-management.component.html',
  styleUrls: ['./spot-management.component.css'],
})
export class SpotManagementComponent implements OnInit, OnDestroy {
  campingId!: number;
  camping: Camping | null = null;
  spots: Spot[] = [];
  loading = true;
  deleteTarget: Spot | null = null;
  deleting = false;
  successMsg: string | null = null;
  errorMsg:   string | null = null;
  private destroy$ = new Subject<void>();

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private spotService: SpotService,
    private campingService: CampingService,
  ) {}

  ngOnInit(): void {
    this.campingId = +this.route.snapshot.params['campingId'];
    this.campingService.getCampingById(this.campingId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({ next: c => this.camping = c });
    this.loadSpots();
  }
  ngOnDestroy(): void { this.destroy$.next(); this.destroy$.complete(); }

  loadSpots(): void {
    this.loading = true;
    this.spotService.getSpotsByCamping(this.campingId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: d => { this.spots = d; this.loading = false; },
        error: () => { this.loading = false; this.errorMsg = 'Failed to load spots.'; },
      });
  }

  confirmDelete(s: Spot): void { this.deleteTarget = s; }
  cancelDelete(): void         { this.deleteTarget = null; }

  executeDelete(): void {
    if (!this.deleteTarget) return;
    this.deleting = true;
    this.spotService.deleteSpot(this.deleteTarget.id!)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.spots = this.spots.filter(s => s.id !== this.deleteTarget!.id);
          this.deleteTarget = null; this.deleting = false;
          this.flash('success', 'Spot deleted.');
        },
        error: () => { this.deleting = false; this.deleteTarget = null; this.flash('error', 'Delete failed.'); },
      });
  }

toggleActive(spot: Spot): void {

  const updated = {
    ...spot,
    active: !spot.active
  };

  const formData = new FormData();

  Object.keys(updated).forEach(key => {

    const value = (updated as any)[key];

    if (value !== null && value !== undefined) {
      formData.append(key, value);
    }

  });

  this.spotService.updateSpot(spot.id!, formData)
    .pipe(takeUntil(this.destroy$))
    .subscribe({

      next: r => {

        const i = this.spots.findIndex(s => s.id === r.id);

        if (i >= 0) {
          this.spots[i] = r;
        }

      },

      error: () =>
        this.flash('error', 'Failed to update spot status.')

    });

}

  private flash(type: 'success'|'error', msg: string): void {
    if (type === 'success') { this.successMsg = msg; this.errorMsg = null; }
    else { this.errorMsg = msg; this.successMsg = null; }
    setTimeout(() => { this.successMsg = null; this.errorMsg = null; }, 4000);
  }
}
