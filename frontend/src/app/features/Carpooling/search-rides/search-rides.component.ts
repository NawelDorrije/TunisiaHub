import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Trip } from '../../../models/Carpooling/carpooling';
import { CarpoolingDataService } from '../services/carpooling-data.service';

@Component({
  selector: 'app-search-rides',
  templateUrl: './search-rides.component.html',
  styleUrls: ['./search-rides.component.css'],
})
export class SearchRidesComponent implements OnInit {
  searchForm!: FormGroup;

  rides: Trip[] = [];

  constructor(
    private fb: FormBuilder,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly dataService: CarpoolingDataService,
  ) {
    this.searchForm = this.fb.group({
      departure: [''],
      destination: [''],
      date: [''],
      seatsNeeded: [1, [Validators.required, Validators.min(1), Validators.max(8)]],
    });
  }

  ngOnInit(): void {
    const params = this.route.snapshot.queryParamMap;
    const departure = params.get('departure') ?? '';
    const destination = params.get('destination') ?? '';
    const date = params.get('date') ?? '';
    const seatsNeeded = Number(params.get('seatsNeeded') ?? 1);

    this.searchForm.patchValue({
      departure,
      destination,
      date,
      seatsNeeded:
        Number.isFinite(seatsNeeded) && seatsNeeded > 0 ? seatsNeeded : 1,
    });

    this.search();
  }

  search(): void {
    const form = this.searchForm.getRawValue();
    this.dataService
      .searchTrips({
        departure: form.departure,
        destination: form.destination,
        date: form.date,
        seatsNeeded: form.seatsNeeded,
      })
      .subscribe({
        next: (rides) => {
          this.rides = rides;
        },
        error: () => {
          this.rides = [];
        },
      });

    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: {
        departure: form.departure || undefined,
        destination: form.destination || undefined,
        date: form.date || undefined,
        seatsNeeded: form.seatsNeeded || 1,
      },
      queryParamsHandling: 'merge',
    });
  }

  viewDetails(tripId: number): void {
    this.router.navigate(['/carpooling/trip', tripId]);
  }

  getDriverName(ownerUserId: number): string {
    return this.dataService.getUserById(ownerUserId).fullName;
  }
}
