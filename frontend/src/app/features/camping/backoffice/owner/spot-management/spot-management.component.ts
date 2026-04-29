import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subject, forkJoin, of } from 'rxjs';
import { takeUntil, catchError } from 'rxjs/operators';
import { Camping } from '../../../../../models/campings/camping';
import { Spot } from '../../../../../models/campings/spot';
import { SpotService } from '../../../../../services/campings/spot.service';
import { CampingService } from '../../../../../services/campings/camping.service';
import {
  DynamicPricingService,
  PricingResponse,
  PricingAudit,
  PriceLevel
} from '../../../../../services/campings/dynamic-pricing.service';
import { ReservationService } from '../../../../../services/shared-reservation/reservation-camping.service';

export interface SpotViewModel extends Spot {
  pricing?: PricingResponse | null;
  audit?: PricingAudit | null;
  reservationCount?: number;
  priceLevel?: PriceLevel;
  pricingLoading?: boolean;
}

@Component({
  selector: 'app-spot-management',
  templateUrl: './spot-management.component.html',
  styleUrls: ['./spot-management.component.css'],
})
export class SpotManagementComponent implements OnInit, OnDestroy {
  campingId!: number;
  camping: Camping | null = null;
  spots: SpotViewModel[] = [];
  loading = true;
  successMsg: string | null = null;
  errorMsg: string | null = null;

  // Modal
  selectedSpot: SpotViewModel | null = null;
  modalOpen = false;
  activePhotoIndex = 0;

  // Today's date for pricing
  readonly today = new Date().toISOString().split('T')[0];

  private destroy$ = new Subject<void>();

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private spotService: SpotService,
    private campingService: CampingService,
    private reservationService: ReservationService,
    private pricingService: DynamicPricingService,
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
        next: spots => {
          this.spots = spots.map(s => ({ ...s, pricingLoading: true }));
          this.loading = false;
          this.loadEnrichedData();
        },
        error: () => {
          this.loading = false;
          this.errorMsg = 'Failed to load spots.';
        },
      });
  }

  private loadEnrichedData(): void {
    this.spots.forEach((spot, index) => {
      if (!spot.id) return;

      forkJoin({
        pricing: this.pricingService.getEffectivePrice(spot.id, this.today).pipe(catchError(() => of(null))),
        audit: this.pricingService.getLatestAudit(spot.id).pipe(catchError(() => of(null))),
        reservations: this.reservationService.getBySpot(spot.id).pipe(catchError(() => of([]))),
      }).pipe(takeUntil(this.destroy$))
        .subscribe(({ pricing, audit, reservations }) => {
          const priceLevel = pricing
            ? this.pricingService.getPriceLevel(pricing.multiplier)
            : undefined;

          this.spots[index] = {
            ...this.spots[index],
            pricing,
            audit,
            reservationCount: Array.isArray(reservations) ? reservations.length : 0,
            priceLevel,
            pricingLoading: false,
          };
        });
    });
  }

  // ── Modal ──────────────────────────────────────────────────────────
  openModal(spot: SpotViewModel): void {
    this.selectedSpot = spot;
    this.activePhotoIndex = 0;
    this.modalOpen = true;
    document.body.style.overflow = 'hidden';
  }

  closeModal(): void {
    this.modalOpen = false;
    this.selectedSpot = null;
    document.body.style.overflow = '';
  }

  nextPhoto(): void {
    if (!this.selectedSpot?.photos?.length) return;
    this.activePhotoIndex = (this.activePhotoIndex + 1) % this.selectedSpot.photos.length;
  }

  prevPhoto(): void {
    if (!this.selectedSpot?.photos?.length) return;
    this.activePhotoIndex =
      (this.activePhotoIndex - 1 + this.selectedSpot.photos.length) % this.selectedSpot.photos.length;
  }

  setPhoto(i: number): void {
    this.activePhotoIndex = i;
  }

  getPhotoUrl(path: string): string {
    return path.startsWith('http') ? path : `http://localhost:8089/uploads/${path}`;
  }

  // ── Delete ─────────────────────────────────────────────────────────
  deleteSpot(spot: SpotViewModel, event: MouseEvent): void {
    event.stopPropagation();
    const msg = `Delete "${spot.name}"? This action is irreversible.`;
    if (!confirm(msg)) return;

    this.spotService.deleteSpot(spot.id!)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.spots = this.spots.filter(s => s.id !== spot.id);
          if (this.selectedSpot?.id === spot.id) this.closeModal();
          this.flash('success', `Spot "${spot.name}" deleted successfully.`);
        },
        error: () => this.flash('error', 'Delete failed. This spot may have active reservations.'),
      });
  }

  // ── Toggle Active ──────────────────────────────────────────────────
  toggleActive(spot: SpotViewModel, event: Event): void {
    event.stopPropagation();
    const updated = { ...spot, active: !spot.active };
    const formData = new FormData();
    Object.keys(updated).forEach(key => {
      const value = (updated as any)[key];
      if (value !== null && value !== undefined && typeof value !== 'object') {
        formData.append(key, String(value));
      }
    });

    this.spotService.updateSpot(spot.id!, formData)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: r => {
          const i = this.spots.findIndex(s => s.id === r.id);
          if (i >= 0) this.spots[i] = { ...this.spots[i], ...r };
        },
        error: () => this.flash('error', 'Failed to update spot status.'),
      });
  }

  // ── Helpers ────────────────────────────────────────────────────────
  getEffectivePrice(spot: SpotViewModel): number {
    return spot.pricing?.dynamicPrice ?? spot.dynamicPrice ?? spot.basePrice;
  }

  getPriceChangePercent(spot: SpotViewModel): number {
    const dynamic = this.getEffectivePrice(spot);
    return this.pricingService.getPriceChangePercent(spot.basePrice, dynamic);
  }

  getOccupancyPercent(spot: SpotViewModel): number {
    return spot.audit ? Math.round(spot.audit.occupancyRate * 100) : 0;
  }

  formatDate(dateStr?: string): string {
    if (!dateStr) return '—';
    return new Date(dateStr).toLocaleDateString('en-GB', {
      day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit'
    });
  }

  trackBySpot(_: number, spot: SpotViewModel): number {
    return spot.id ?? _;
  }

  private flash(type: 'success' | 'error', msg: string): void {
    if (type === 'success') { this.successMsg = msg; this.errorMsg = null; }
    else { this.errorMsg = msg; this.successMsg = null; }
    setTimeout(() => { this.successMsg = null; this.errorMsg = null; }, 4500);
  }
}
