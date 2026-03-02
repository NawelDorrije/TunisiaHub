import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { SessionService } from '../../../../core/services/session.service';
import { Trip } from '../../models/trip.model';
import { TripService } from '../../services/trip.service';

@Component({
  selector: 'app-trip-details',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './trip-details.component.html',
  styleUrl: './trip-details.component.css',
})
export class TripDetailsComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly tripService = inject(TripService);
  private readonly sessionService = inject(SessionService);

  protected readonly loading = signal(false);
  protected readonly errorMessage = signal('');
  protected readonly trip = signal<Trip | null>(null);

  protected readonly isOwnerDriver = computed(() => {
    const currentTrip = this.trip();
    if (!currentTrip) {
      return false;
    }

    return (
      this.sessionService.role() === 'DRIVER' &&
      this.sessionService.userId() === currentTrip.driverId
    );
  });

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!Number.isFinite(id) || id <= 0) {
      this.errorMessage.set('Invalid trip id');
      return;
    }

    this.loadTrip(id);
  }

  cancelTrip(): void {
    const currentTrip = this.trip();
    if (!currentTrip) {
      return;
    }

    this.errorMessage.set('');
    this.tripService.cancelTrip(currentTrip.id).subscribe({
      next: (updated) => this.trip.set(updated),
      error: (error) => this.errorMessage.set(error?.error?.message ?? 'Failed to cancel trip'),
    });
  }

  formatDateTime(value: string): string {
    return new Date(value).toLocaleString();
  }

  private loadTrip(id: number): void {
    this.loading.set(true);
    this.errorMessage.set('');

    this.tripService.getTripById(id).subscribe({
      next: (trip) => {
        this.trip.set(trip);
        this.loading.set(false);
      },
      error: (error) => {
        this.errorMessage.set(error?.error?.message ?? 'Failed to load trip details');
        this.loading.set(false);
      },
    });
  }
}
