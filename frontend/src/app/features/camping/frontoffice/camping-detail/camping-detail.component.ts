import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { Camping } from '../../../../models/campings/camping';
import { Spot } from '../../../../models/campings/spot';
import { Activity } from '../../../../models/campings/activity';
import { Equipment } from '../../../../models/campings/equipment';
import { CampingService } from '../../../../services/campings/camping.service';
import { SpotService } from '../../../../services/campings/spot.service';
import { ActivityService } from '../../../../services/campings/activity.service';
import { EquipmentService } from '../../../../services/campings/equipment.service';

@Component({
  selector: 'app-camping-detail',
  templateUrl: './camping-detail.component.html',
  styleUrls: ['./camping-detail.component.css']
})
export class CampingDetailComponent implements OnInit, OnDestroy {
  camping!: Camping;
  spots: Spot[] = [];
  activities: Activity[] = [];
  equipment: Equipment[] = [];

  loading = true;
  error = '';
  activePhoto = 0;
  activeTab: 'overview' | 'spots' | 'activities' | 'equipment' | 'rules' = 'overview';

  private destroy$ = new Subject<void>();

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private campingService: CampingService,
    private spotService: SpotService,
    private activityService: ActivityService,
    private equipmentService: EquipmentService
  ) {}

  ngOnInit(): void {
    const id = +this.route.snapshot.params['id'];
    this.loadCamping(id);
    this.loadSpots(id);
    this.loadActivities(id);
  }

  private loadCamping(id: number): void {
    this.campingService.getCampingById(id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data) => { this.camping = data; this.loading = false; },
        error: () => {
          this.error = 'Could not load camping details. Please try again.';
          this.loading = false;
        }
      });
  }

  private loadSpots(id: number): void {
    this.spotService.getSpotsByCamping(id)
      .pipe(takeUntil(this.destroy$))
      .subscribe(spots => {
        this.spots = spots.filter(s => s.active);
        this.spots.forEach(spot => {
          if (spot.id) {
            this.equipmentService.getEquipmentBySpot(spot.id)
              .pipe(takeUntil(this.destroy$))
              .subscribe(eq => { this.equipment = [...this.equipment, ...eq]; });
          }
        });
      });
  }

  private loadActivities(id: number): void {
    this.activityService.getActivitiesByCamping(id)
      .pipe(takeUntil(this.destroy$))
      .subscribe(acts => this.activities = acts.filter(a => a.active));
  }

  goToReserve(): void {
    this.router.navigate(['/camping', this.camping.id, 'reserve']);
  }

  goBack(): void {
    this.router.navigate(['/camping']);
  }

  setPhoto(idx: number): void { this.activePhoto = idx; }

  setTab(tab: 'overview' | 'spots' | 'activities' | 'equipment' | 'rules'): void {
    this.activeTab = tab;
  }

  getStars(rating: number = 0): string[] {
    const full = Math.floor(rating);
    const half = rating % 1 >= 0.5 ? 1 : 0;
    const empty = 5 - full - half;
    return [
      ...Array(full).fill('full'),
      ...Array(half).fill('half'),
      ...Array(empty).fill('empty')
    ];
  }

  getSpotTypeLabel(type: string): string {
    const labels: Record<string, string> = {
      TENT: 'Tent', CARAVAN: 'Caravan', BUNGALOW: 'Bungalow',
      TREEHOUSE: 'Treehouse', GLAMPING: 'Glamping', MOBILE_HOME: 'Mobile Home'
    };
    return labels[type] || type;
  }

  getViewTypeLabel(type: string): string {
    const labels: Record<string, string> = {
      SEA: 'Sea View', LAKE: 'Lake View', MOUNTAIN: 'Mountain View',
      FOREST: 'Forest View', STANDARD: 'Standard'
    };
    return labels[type] || type;
  }

  getConditionClass(condition: string): string {
    const map: Record<string, string> = {
      GOOD: 'cond-good', DAMAGED: 'cond-damaged', UNDER_REPAIR: 'cond-repair'
    };
    return map[condition] || '';
  }

  getConditionLabel(condition: string): string {
    const map: Record<string, string> = {
      GOOD: 'Good Condition', DAMAGED: 'Damaged', UNDER_REPAIR: 'Under Repair'
    };
    return map[condition] || condition;
  }

  get uniqueEquipment(): Equipment[] {
    const seen = new Set<string>();
    return this.equipment.filter(e => {
      const key = `${e.name}-${e.condition}`;
      return seen.has(key) ? false : (seen.add(key), true);
    });
  }

  get isActive(): boolean {
    return this.camping?.status === 'ACTIVE';
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
