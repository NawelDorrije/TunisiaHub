import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { forkJoin, of, Subject } from 'rxjs';
import { switchMap, takeUntil, catchError } from 'rxjs/operators';
import { CampingService } from '../../../../../services/campings/camping.service';
import { SpotService } from '../../../../../services/campings/spot.service';
import { ActivityService } from '../../../../../services/campings/activity.service';
import { EquipmentService } from '../../../../../services/campings/equipment.service';
import { ReservationService } from '../../../../../services/shared-reservation/reservation-camping.service';
import { DynamicPricingService } from '../../../../../services/campings/dynamic-pricing.service';
import { Camping } from '../../../../../models/campings/camping';
import { Spot } from '../../../../../models/campings/spot';
import { Reservation } from '../../../../../models/shared-reservation/reservation';
import { AuthService } from '../../../../auth/services/auth.service';

interface KpiCard {
  label: string;
  value: string | number;
  sub: string;
  icon: string;
  iconBg: string;
  change?: string;
  changeUp?: boolean;
}

interface SpotPerf {
  name: string;
  campingName: string;
  reservations: number;
  revenue: number;
  dynamicPrice?: number;
  basePrice: number;
  multiplier?: number;
}

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit, OnDestroy {

  today = new Date();
  loading = true;
  Math = Math;
  private destroy$ = new Subject<void>();
   userId!: number;

  campings: Camping[] = [];
  selectedCampingId: number | null = null;
  selectedStatus = '';
  dateFrom = '';
  dateTo = '';

  kpis: KpiCard[] = [];
  allSpots: Spot[] = [];
  allReservations: Reservation[] = [];
  recentReservations: Reservation[] = [];
  spotsByType: { type: string; count: number }[] = [];
  spotPerformance: SpotPerf[] = [];
  topActivities: { name: string; count: number }[] = [];

  revenueLabels: string[] = [];
  revenueData: number[] = [];
  resLabels: string[] = [];
  resData: number[] = [];
  topSpotLabels: string[] = [];
  topSpotData: number[] = [];
  occupancyData: number[] = [0, 0];

  avgPriceBoost = 0;

  private chartInstances = new Map<string, any>();

  // ── Template helpers ───────────────────────────────────────────────

  get pricedSpotsLabel(): string {
    const priced = this.allSpots.filter(s => s.dynamicPrice).length;
    return `${priced} / ${this.allSpots.length} priced`;
  }

  get firstCampingId(): number {
    return this.campings[0]?.id ?? 0;
  }

  get topSpotChartHeight(): number {
    return Math.max(160, this.topSpotLabels.length * 36 + 40);
  }

  constructor(
    private campingService: CampingService,
    private spotService: SpotService,
    private activityService: ActivityService,
    private equipmentService: EquipmentService,
    private reservationService: ReservationService,
    private pricingService: DynamicPricingService,
    private router: Router,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
     const id = this.authService.getId();
    if (!id) {
      this.router.navigate(['/auth/sign-in']);
      return;
    }
    this.userId = id;
    this.loadDashboard();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    // Destroy all chart instances to prevent memory leaks
    this.chartInstances.forEach(c => c.destroy());
  }

  // ── Data loading ─────────────────────────────────────────────────

  loadDashboard(): void {
    this.loading = true;

    this.campingService.getByOwner(this.userId).pipe(
      takeUntil(this.destroy$),
      switchMap(campings => {
        this.campings = campings;
        if (!campings.length) {
          return of({ spots: [], activities: [], equipment: [], reservations: [] });
        }
        const ids = campings.map(c => c.id!);
        return forkJoin({
          spots:        forkJoin(ids.map(id => this.spotService.getSpotsByCamping(id).pipe(catchError(() => of([]))))),
          activities:   forkJoin(ids.map(id => this.activityService.getActivitiesByCamping(id).pipe(catchError(() => of([]))))),
          equipment:    forkJoin(ids.map(id => this.equipmentService.getEquipmentByCamping(id).pipe(catchError(() => of([]))))),
          reservations: this.reservationService.getAllReservations().pipe(catchError(() => of([])))
        });
      })
    ).subscribe({
      next: (result: any) => {
        this.allSpots        = (result.spots?.flat()      || []) as Spot[];
        const allActivities  = (result.activities?.flat() || []) as any[];
        const allEquipment   = (result.equipment?.flat()  || []) as any[];
        this.allReservations = (result.reservations       || []) as Reservation[];

        this.processData(allActivities, allEquipment);
        this.loading = false;

        // ── KEY FIX: wait for Angular to render canvases, then draw ──
        setTimeout(() => this.initCharts(), 0);
      },
      error: () => { this.loading = false; }
    });
  }

  // ── Data processing ───────────────────────────────────────────────

  private processData(allActivities: any[], allEquipment: any[]): void {
    const spots       = this.filteredSpots();
    const filteredRes = this.filteredReservations();

    const activeCampings      = this.campings.filter(c => c.status === 'ACTIVE').length;
    const pendingCampings     = this.campings.filter(c => c.status === 'PENDING').length;
    const availableSpots      = spots.filter(s => s.status === 'LIBRE').length;
    const confirmedRes        = filteredRes.filter(r => r.status === 'CONFIRMED' || r.status === 'COMPLETED');
    const pendingRes          = filteredRes.filter(r => r.status === 'PENDING');
    const totalRevenue        = confirmedRes.reduce((s, r) => s + (r.totalPrice || 0), 0);
    const pendingPaymentTotal = pendingRes.reduce((s, r) => s + (r.totalPrice || 0), 0);
    const occupancyRate       = spots.length
      ? Math.round(((spots.length - availableSpots) / spots.length) * 100)
      : 0;

    const resBySpot: Record<number, number> = {};
    filteredRes.forEach(r => { resBySpot[r.spotId] = (resBySpot[r.spotId] || 0) + 1; });
    const topSpotId = Object.entries(resBySpot).sort((a, b) => b[1] - a[1])[0]?.[0];
    const topSpot   = spots.find(s => s.id === Number(topSpotId));

    this.kpis = [
      { label: 'My campings',     value: this.campings.length,          sub: `${activeCampings} active · ${pendingCampings} pending`, icon: '🏕️', iconBg: '#E8F5E9', change: `${activeCampings} active`, changeUp: true },
      { label: 'Total spots',     value: spots.length,                   sub: `${availableSpots} available now`,    icon: '📍', iconBg: '#E3F2FD', change: `${occupancyRate}% full`,   changeUp: occupancyRate > 50 },
      { label: 'Reservations',    value: filteredRes.length,             sub: `${pendingRes.length} pending action`, icon: '📋', iconBg: '#F3E5F5', change: `${pendingRes.length} open`, changeUp: false },
      { label: 'Revenue (TND)',   value: Math.round(totalRevenue),       sub: 'confirmed only',                     icon: '💰', iconBg: '#FFF8E1', change: '+12%',                     changeUp: true },
      { label: 'Pending payments',value: Math.round(pendingPaymentTotal),sub: 'TND outstanding',                   icon: '⏳', iconBg: '#FFF3E0', change: `${pendingRes.length} open`, changeUp: false },
      { label: 'Occupancy rate',  value: `${occupancyRate}%`,            sub: `${spots.length - availableSpots} of ${spots.length}`, icon: '📊', iconBg: '#E8F5E9', change: occupancyRate >= 50 ? 'Above 50%' : 'Below 50%', changeUp: occupancyRate >= 50 },
      { label: 'Activities',      value: allActivities.length,           sub: 'across all campings',               icon: '🎯', iconBg: '#E0F7FA', change: `+${allActivities.length}`,  changeUp: true },
      { label: 'Equipment',       value: allEquipment.length,            sub: `${allEquipment.filter(e => e.available).length} available`, icon: '🛠️', iconBg: '#FCE4EC' },
      { label: 'Top spot',        value: topSpot?.name || '—',           sub: `${resBySpot[Number(topSpotId)] || 0} reservations`,        icon: '⭐', iconBg: '#F3E5F5' },
    ];

    // Spot type breakdown
    const typeMap: Record<string, number> = {};
    spots.forEach(s => { typeMap[s.type] = (typeMap[s.type] || 0) + 1; });
    this.spotsByType = Object.entries(typeMap)
      .map(([type, count]) => ({ type, count }))
      .sort((a, b) => b.count - a.count);

    // Recent reservations
    this.recentReservations = [...filteredRes]
      .sort((a, b) => new Date(b.createdAt || 0).getTime() - new Date(a.createdAt || 0).getTime())
      .slice(0, 8);

    // Spot performance
    this.spotPerformance = spots.map(s => {
      const spotRes = filteredRes.filter(r => r.spotId === s.id);
      const rev = spotRes.filter(r => r.status === 'CONFIRMED' || r.status === 'COMPLETED')
        .reduce((sum, r) => sum + (r.totalPrice || 0), 0);
      return {
        name: s.name,
        campingName: s.campingName || '—',
        reservations: spotRes.length,
        revenue: Math.round(rev),
        dynamicPrice: s.dynamicPrice,
        basePrice: s.basePrice,
        multiplier: s.dynamicPrice ? s.dynamicPrice / s.basePrice : undefined
      };
    }).sort((a, b) => b.reservations - a.reservations).slice(0, 8);

    // Top activities
    const actMap: Record<string, number> = {};
    filteredRes.forEach(r => r.activityNames?.forEach((n: string) => {
      actMap[n] = (actMap[n] || 0) + 1;
    }));
    this.topActivities = Object.entries(actMap)
      .map(([name, count]) => ({ name, count }))
      .sort((a, b) => b.count - a.count).slice(0, 5);

    // Revenue chart (last 7 days)
    const days = Array.from({ length: 7 }, (_, i) => {
      const d = new Date();
      d.setDate(d.getDate() - (6 - i));
      return d;
    });
    this.revenueLabels = days.map(d => d.toLocaleDateString('en-GB', { weekday: 'short' }));
    this.revenueData   = days.map(d => {
      const dayStr = d.toISOString().split('T')[0];
      return Math.round(confirmedRes
        .filter(r => r.checkIn?.startsWith(dayStr))
        .reduce((s, r) => s + (r.totalPrice || 0), 0));
    });

    // Reservations chart (last 7 days)
    this.resLabels = this.revenueLabels;
    this.resData   = days.map(d => {
      const dayStr = d.toISOString().split('T')[0];
      return filteredRes.filter(r => r.checkIn?.startsWith(dayStr)).length;
    });

    // Top spots chart
    this.topSpotLabels = this.spotPerformance.slice(0, 5).map(s => s.name);
    this.topSpotData   = this.spotPerformance.slice(0, 5).map(s => s.reservations);

    // Occupancy donut
    this.occupancyData = [spots.length - availableSpots, availableSpots];

    // AI pricing
    const priced = spots.filter(s => s.dynamicPrice && s.basePrice);
    this.avgPriceBoost = priced.length
      ? Math.round(priced.reduce((s, sp) =>
          s + ((sp.dynamicPrice! - sp.basePrice) / sp.basePrice) * 100, 0) / priced.length)
      : 0;
  }

  // ── Chart rendering ── THE FIXED METHOD ───────────────────────────
  // Called via setTimeout(() => this.initCharts(), 0) AFTER loading=false
  // so Angular has already rendered the *ngIf="!loading" block and the
  // canvas elements exist in the DOM.

  private initCharts(): void {
    const isDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
    const gridC  = isDark ? 'rgba(255,255,255,0.06)' : 'rgba(0,0,0,0.05)';
    const textC  = isDark ? '#888' : '#aaa';
    const tick   = { color: textC, font: { size: 10 } };

    this.buildChart('revChart', 'line', this.revenueLabels, [{
      label: 'Revenue (TND)', data: this.revenueData,
      borderColor: '#2e7d32', backgroundColor: 'rgba(46,125,50,0.07)',
      tension: 0.4, pointRadius: 3, pointBackgroundColor: '#2e7d32', fill: true
    }], {
      x: { grid: { color: gridC }, ticks: { ...tick, autoSkip: false } },
      y: { grid: { color: gridC }, ticks: { ...tick, callback: (v: any) => v + 'T' } }
    });

    this.buildChart('occChart', 'doughnut', ['Occupied', 'Free'], [{
      data: this.occupancyData,
      backgroundColor: ['#2e7d32', '#e0e0e0'],
      borderWidth: 0, hoverOffset: 4
    }], {}, { cutout: '72%' });

    this.buildChart('resChart', 'bar', this.resLabels, [{
      label: 'Reservations', data: this.resData,
      backgroundColor: '#00796b', borderRadius: 4
    }], {
      x: { grid: { display: false }, ticks: { ...tick, autoSkip: false } },
      y: { grid: { color: gridC }, ticks: { ...tick, stepSize: 2 } }
    });

    this.buildChart('spotChart', 'bar', this.topSpotLabels, [{
      label: 'Reservations', data: this.topSpotData,
      backgroundColor: '#6a1b9a', borderRadius: 4
    }], {
      x: { grid: { color: gridC }, ticks: tick },
      y: { grid: { display: false }, ticks: tick }
    }, { indexAxis: 'y' });
  }

  private buildChart(id: string, type: string, labels: string[],
                     datasets: any[], scales: any = {}, extra: any = {}): void {
    const canvas = document.getElementById(id) as HTMLCanvasElement;
    if (!canvas) {
      console.warn(`[Dashboard] canvas #${id} not found — chart skipped`);
      return;
    }
    if (this.chartInstances.has(id)) {
      this.chartInstances.get(id).destroy();
    }
    const instance = new (window as any).Chart(canvas, {
      type,
      data: { labels, datasets },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { display: false } },
        scales,
        ...extra
      }
    });
    this.chartInstances.set(id, instance);
  }

  // ── Filters ───────────────────────────────────────────────────────

  filteredSpots(): Spot[] {
    return this.selectedCampingId
      ? this.allSpots.filter(s => s.campingId === this.selectedCampingId)
      : this.allSpots;
  }

  filteredReservations(): Reservation[] {
    let res = this.allReservations;
    if (this.selectedCampingId) {
      const ids = new Set(
        this.allSpots.filter(s => s.campingId === this.selectedCampingId).map(s => s.id!)
      );
      res = res.filter(r => ids.has(r.spotId));
    }
    if (this.selectedStatus) res = res.filter(r => r.status === this.selectedStatus);
    if (this.dateFrom)       res = res.filter(r => r.checkIn >= this.dateFrom);
    if (this.dateTo)         res = res.filter(r => r.checkIn <= this.dateTo);
    return res;
  }

  onFilterChange(): void {
    // Reprocess data then redraw charts
    this.processData([], []);
    setTimeout(() => this.initCharts(), 0);
  }

  resetFilters(): void {
    this.selectedCampingId = null;
    this.selectedStatus    = '';
    this.dateFrom          = '';
    this.dateTo            = '';
    this.onFilterChange();
  }

  // ── Helpers ───────────────────────────────────────────────────────

  getSpotTypeIcon(type: string): string {
    const m: Record<string, string> = {
      TENT: '⛺', CARAVAN: '🚐', BUNGALOW: '🏠',
      TREEHOUSE: '🌲', GLAMPING: '✨', MOBILE_HOME: '🏡'
    };
    return m[type] || '📍';
  }

  getStatusClass(status: string): string {
    const m: Record<string, string> = {
      CONFIRMED: 'b-confirmed', ACTIVE: 'b-active', PAID: 'b-confirmed',
      PENDING: 'b-pending', CANCELLED: 'b-cancelled', COMPLETED: 'b-completed'
    };
    return m[status] || 'b-neutral';
  }

  getPriceLevelIcon(multiplier: number): string {
    if (multiplier >= 1.4) return '🔥';
    if (multiplier >= 1.2) return '📈';
    if (multiplier >= 1.05) return '☀️';
    return '⚖️';
  }

  navigate(path: (string | number)[]): void {
    this.router.navigate(path);
  }

  navigateToSpots(): void {
    const id = this.selectedCampingId ?? this.firstCampingId;
    if (id) this.router.navigate(['/camping/backoffice/owner', id, 'spots']);
  }

  navigateToCampingSpots(campingId: number | undefined): void {
    if (campingId != null) {
      this.router.navigate(['/camping/backoffice/owner', campingId, 'spots']);
    }
  }
}
