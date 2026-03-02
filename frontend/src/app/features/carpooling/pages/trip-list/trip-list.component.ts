import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Trip } from '../../models/trip.model';
import { TripService } from '../../services/trip.service';

@Component({
  selector: 'app-trip-list',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './trip-list.component.html',
  styleUrl: './trip-list.component.css',
})
export class TripListComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly tripService = inject(TripService);
  private readonly route = inject(ActivatedRoute);

  protected readonly searchForm = this.fb.group({
    departurePoint: [''],
    destination: [''],
    date: [''],
  });

  protected trips: Trip[] = [];
  protected loading = false;
  protected errorMessage = '';
  protected infoMessage = '';

  ngOnInit(): void {
    this.route.queryParamMap.subscribe((params) => {
      this.infoMessage = params.get('denied') === 'driver' ? 'Access denied (Driver only).' : '';
    });
    this.searchTrips();
  }

  searchTrips(): void {
    this.loading = true;
    this.errorMessage = '';

    const value = this.searchForm.getRawValue();

    this.tripService
      .listTrips({
        departurePoint: value.departurePoint ?? undefined,
        destination: value.destination ?? undefined,
        date: value.date ?? undefined,
      })
      .subscribe({
        next: (trips) => {
          this.trips = trips.filter((trip) => trip.status !== 'CANCELLED');
          this.loading = false;
        },
        error: (error) => {
          this.errorMessage = error?.error?.message ?? 'Failed to load trips';
          this.loading = false;
        },
      });
  }

  formatDateTime(value: string): string {
    return new Date(value).toLocaleString();
  }
}
