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

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadSpots(): void {
    this.loading = true;
    this.spotService.getSpotsByCamping(this.campingId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: d => { this.spots = d; this.loading = false; },
        error: () => {
          this.loading = false;
          this.errorMsg = 'Failed to load spots.';
        },
      });
  }

  // ==================== SIMPLE CONFIRM DELETE ====================
  deleteSpot(spot: Spot): void {
    const confirmMessage = `Voulez-vous vraiment supprimer le spot "${spot.name}" ?
Cette action est irréversible.`;

    if (!confirm(confirmMessage)) {
      return; // User clicked Cancel
    }

    // User confirmed → proceed with deletion
    this.spotService.deleteSpot(spot.id!)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.spots = this.spots.filter(s => s.id !== spot.id);
          this.flash('success', `Spot "${spot.name}" supprimé avec succès.`);
        },
        error: (err) => {
          console.error(err);
          this.flash('error', 'Échec de la suppression. Ce spot est peut-être utilisé par des réservations.');
        }
      });
  }

  // ==================== Existing methods (unchanged) ====================
  toggleActive(spot: Spot): void {
    const updated = { ...spot, active: !spot.active };

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
          if (i >= 0) this.spots[i] = r;
        },
        error: () => this.flash('error', 'Failed to update spot status.')
      });
  }

  private flash(type: 'success'|'error', msg: string): void {
    if (type === 'success') {
      this.successMsg = msg;
      this.errorMsg = null;
    } else {
      this.errorMsg = msg;
      this.successMsg = null;
    }
    setTimeout(() => {
      this.successMsg = null;
      this.errorMsg = null;
    }, 4000);
  }
}
