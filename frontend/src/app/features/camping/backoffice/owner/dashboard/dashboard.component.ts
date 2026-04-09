import { Component, OnInit } from '@angular/core';
import { CampingService } from '../../../../../services/campings/camping.service';
import { SpotService } from '../../../../../services/campings/spot.service';
import { ActivityService } from '../../../../../services/campings/activity.service';
import { EquipmentService } from '../../../../../services/campings/equipment.service';
import { ReservationService } from '../../../../../services/shared-reservation/reservation-camping.service';
import { forkJoin, of, switchMap } from 'rxjs';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  today: Date = new Date();

  // Hardcoded owner ID — replace with AuthService.getCurrentUser().id in production
  private ownerId = 1;

  stats = {
    totalCampings: 0,
    activeCampings: 0,
    pendingCampings: 0,
    totalSpots: 0,
    availableSpots: 0,
    totalActivities: 0,
    totalEquipment: 0,
    totalReservations: 0,
    pendingReservations: 0,
    totalRevenue: 0
  };

  recentCampings: any[] = [];
  recentReservations: any[] = [];
  spotsByType: { type: string; count: number }[] = [];
  loading = true;

  constructor(
    private campingService: CampingService,
    private spotService: SpotService,
    private activityService: ActivityService,
    private equipmentService: EquipmentService,
    private reservationService: ReservationService
  ) {}

  ngOnInit(): void {
    // Step 1: load owner's campings
    this.campingService.getByOwner(this.ownerId).pipe(
      switchMap(campings => {
        this.stats.totalCampings = campings.length;
        this.stats.activeCampings = campings.filter(c => c.status === 'ACTIVE').length;
        this.stats.pendingCampings = campings.filter(c => c.status === 'PENDING').length;
        this.recentCampings = campings.slice(0, 5);

        if (!campings.length) return of({ spots: [], activities: [], equipment: [], reservations: [] });

        const campingIds = campings.map(c => c.id!);

        // Step 2: load spots/activities/equipment/reservations for all campings
        return forkJoin({
          spots: forkJoin(campingIds.map(id => this.spotService.getSpotsByCamping(id))),
          activities: forkJoin(campingIds.map(id => this.activityService.getActivitiesByCamping(id))),
          equipment: of([]), // equipment is per-spot — simplified here
          reservations: this.reservationService.getAllReservations()
        });
      })
    ).subscribe({
      next: (result: any) => {
        const allSpots = result.spots?.flat() || [];
        const allActivities = result.activities?.flat() || [];
        const allReservations = result.reservations || [];

        this.stats.totalSpots = allSpots.length;
        this.stats.availableSpots = allSpots.filter((s: any) => s.status === 'LIBRE').length;
        this.stats.totalActivities = allActivities.length;
        this.stats.totalReservations = allReservations.length;
        this.stats.pendingReservations = allReservations.filter((r: any) => r.status === 'PENDING').length;
        this.stats.totalRevenue = allReservations
          .filter((r: any) => r.status === 'CONFIRMED' || r.status === 'COMPLETED')
          .reduce((sum: number, r: any) => sum + (r.totalPrice || 0), 0);

        this.recentReservations = allReservations.slice(0, 5);

        // Spot type distribution
        const typeMap: Record<string, number> = {};
        allSpots.forEach((s: any) => {
          typeMap[s.type] = (typeMap[s.type] || 0) + 1;
        });
        this.spotsByType = Object.entries(typeMap).map(([type, count]) => ({ type, count }));

        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  getSpotTypeIcon(type: string): string {
    const icons: Record<string, string> = {
      TENT: '⛺', CARAVAN: '🚐', BUNGALOW: '🏡',
      TREEHOUSE: '🌳', GLAMPING: '✨', MOBILE_HOME: '🏠'
    };
    return icons[type] || '📍';
  }
}
