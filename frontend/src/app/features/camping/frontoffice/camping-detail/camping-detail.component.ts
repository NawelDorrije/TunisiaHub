import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { Camping } from '../../../../models/campings/camping';
import { Spot } from '../../../../models/campings/spot';
import { Activity } from '../../../../models/campings/activity';
import { CampingService } from '../../../../services/campings/camping.service';
import { SpotService } from '../../../../services/campings/spot.service';
import { ActivityService } from '../../../../services/campings/activity.service';

@Component({
  selector: 'app-camping-detail',
  templateUrl: './camping-detail.component.html',
  styleUrls: ['./camping-detail.component.css']
})
export class CampingDetailComponent implements OnInit, OnDestroy {
  camping!: Camping;
  spots: Spot[] = [];
  activities: Activity[] = [];
  loading = true;
  error = '';
  activePhoto = 0;
  private destroy$ = new Subject<void>();

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private campingService: CampingService,
    private spotService: SpotService,
    private activityService: ActivityService
  ) {}

  ngOnInit(): void {
    const id = +this.route.snapshot.params['id'];
    this.campingService.getCampingById(id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data) => {
          this.camping = data;
          this.loading = false;
        },
        error: () => {
          this.error = 'Could not load camping details.';
          this.loading = false;
        }
      });

    this.spotService.getSpotsByCamping(id)
      .pipe(takeUntil(this.destroy$))
      .subscribe(spots => this.spots = spots.filter(s => s.active));

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

  setPhoto(idx: number): void {
    this.activePhoto = idx;
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

  getSpotTypeIcon(type: string): string {
    const icons: Record<string, string> = {
      TENT: '⛺', CARAVAN: '🚐', BUNGALOW: '🏠',
      TREEHOUSE: '🌳', GLAMPING: '✨', MOBILE_HOME: '🏡'
    };
    return icons[type] || '🏕';
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
