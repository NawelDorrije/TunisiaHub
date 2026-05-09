<<<<<<< HEAD
import { Component } from '@angular/core';
import { inject } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
=======
import { Component, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { DemandAlert } from '../../../models/Carpooling/carpooling';
import { AuthService } from '../../auth/services/auth.service';
import { CarpoolingDataService } from '../services/carpooling-data.service';

interface LocationSuggestion {
  label: string;
  value: string;
  type: 'city';
}
>>>>>>> origin/feature/integrated-app-event

@Component({
  selector: 'app-carpooling-home',
  templateUrl: './carpooling-home.component.html',
  styleUrls: ['./carpooling-home.component.css'],
})
<<<<<<< HEAD
export class CarpoolingHomeComponent {
  private readonly fb = inject(FormBuilder);

  readonly searchForm = this.fb.nonNullable.group({
    departure: [''],
    destination: [''],
    date: [''],
    seatsNeeded: [
      1,
      [Validators.required, Validators.min(1), Validators.max(8)],
    ],
  });

  constructor(private readonly router: Router) {}

  searchRide(): void {
    const form = this.searchForm.getRawValue();
    this.router.navigate(['/carpooling/search-rides'], {
      queryParams: {
        departure: form.departure || undefined,
        destination: form.destination || undefined,
        date: form.date || undefined,
        seatsNeeded: form.seatsNeeded || 1,
=======
export class CarpoolingHomeComponent implements OnDestroy {
  searchForm!: FormGroup;
  departureSuggestions: LocationSuggestion[] = [];
  destinationSuggestions: LocationSuggestion[] = [];
  showDepartureSuggestions = false;
  showDestinationSuggestions = false;
  loadingLocations = false;
  locationError = '';
  demandAlert?: DemandAlert;
  demandAlertLoading = false;
  demandAlertError = '';
  private searchTimeout: any = null;
  private demandTimeout: any = null;
  private formSubscription?: Subscription;

  constructor(
    private fb: FormBuilder,
    private router: Router,
    public authService: AuthService,
    private dataService: CarpoolingDataService,
  ) {
    this.searchForm = this.fb.group({
      departure: [''],
      destination: [''],
      date: [''],
      returnDate: [''],
      seatsNeeded: [1, [Validators.required, Validators.min(1), Validators.max(8)]],
    });

    this.formSubscription = this.searchForm.valueChanges.subscribe(() => {
      this.scheduleDemandAlert();
    });
  }

  get f() {
    return this.searchForm.controls;
  }

  ngOnDestroy(): void {
    if (this.searchTimeout) {
      clearTimeout(this.searchTimeout);
    }
    if (this.demandTimeout) {
      clearTimeout(this.demandTimeout);
    }
    this.formSubscription?.unsubscribe();
  }

  searchRide(): void {
    if (this.searchForm.invalid) {
      this.searchForm.markAllAsTouched();
      return;
    }

    this.router.navigate(['/carpooling/search-rides'], {
      queryParams: {
        departure: this.searchForm.value.departure || undefined,
        destination: this.searchForm.value.destination || undefined,
        date: this.searchForm.value.date || undefined,
        returnDate: this.searchForm.value.returnDate || undefined,
        seatsNeeded: this.searchForm.value.seatsNeeded || 1,
>>>>>>> origin/feature/integrated-app-event
      },
    });
  }

<<<<<<< HEAD
  publishRide(): void {
    this.router.navigate(['/carpooling/publish']);
  }
=======
  swapLocations(): void {
    const departure = this.searchForm.value.departure || '';
    const destination = this.searchForm.value.destination || '';

    this.searchForm.patchValue({
      departure: destination,
      destination: departure,
    });
  }

  publishRide(): void {
    this.router.navigate(['/carpooling/publish']);
  }

  onLocationInput(field: 'departure' | 'destination'): void {
    const value = `${this.searchForm.value[field] || ''}`.trim();
    this.locationError = '';

    if (this.searchTimeout) {
      clearTimeout(this.searchTimeout);
    }

    if (!value) {
      this.loadingLocations = false;
      if (field === 'departure') {
        this.departureSuggestions = [];
        this.showDepartureSuggestions = false;
        return;
      }

      this.destinationSuggestions = [];
      this.showDestinationSuggestions = false;
      return;
    }

    if (field === 'departure') {
      this.showDepartureSuggestions = true;
    } else {
      this.showDestinationSuggestions = true;
    }

    this.searchTimeout = setTimeout(() => {
      this.searchTunisiaCities(field, value);
    }, 250);
  }

  openSuggestions(field: 'departure' | 'destination'): void {
    if (`${this.searchForm.value[field] || ''}`.trim()) {
      this.onLocationInput(field);
    }
  }

  closeSuggestions(field: 'departure' | 'destination'): void {
    setTimeout(() => {
      if (field === 'departure') {
        this.showDepartureSuggestions = false;
        return;
      }

      this.showDestinationSuggestions = false;
    }, 150);
  }

  selectSuggestion(
    field: 'departure' | 'destination',
    suggestion: LocationSuggestion
  ): void {
    this.searchForm.patchValue({
      [field]: suggestion.value,
    });

    if (field === 'departure') {
      this.departureSuggestions = [];
      this.showDepartureSuggestions = false;
      return;
    }

    this.destinationSuggestions = [];
    this.showDestinationSuggestions = false;
  }

  getDemandLevelLabel(): string {
    if (!this.demandAlert) {
      return '';
    }

    if (this.demandAlert.holidayCriticalWarning) {
      return 'Holiday alert';
    }
    if (this.demandAlert.demandLevel === 'high') {
      return 'High demand';
    }
    if (this.demandAlert.demandLevel === 'medium') {
      return 'Medium demand';
    }
    return 'Low demand';
  }

  getDemandOccupancyPercent(): number {
    if (!this.demandAlert) {
      return 0;
    }

    return Math.round((this.demandAlert.predictedOccupancyRate || 0) * 100);
  }

  shouldShowDemandAlert(): boolean {
    return !!this.demandAlert
      && (this.demandAlert.demandLevel === 'high' || this.demandAlert.holidayCriticalWarning);
  }

  shouldShowNoResults(field: 'departure' | 'destination'): boolean {
    const value = `${this.searchForm.value[field] || ''}`.trim();
    const suggestions =
      field === 'departure'
        ? this.departureSuggestions
        : this.destinationSuggestions;

    return !this.loadingLocations && !this.locationError && value.length > 0 && suggestions.length === 0;
  }

  private searchTunisiaCities(
    field: 'departure' | 'destination',
    value: string,
  ): void {
    this.loadingLocations = true;
    this.dataService.searchTunisiaCities(value).subscribe({
      next: (cities: string[]) => {
        const suggestions = cities.map((city) => ({
          label: city,
          value: city,
          type: 'city' as const,
        }));

        if (field === 'departure') {
          this.departureSuggestions = suggestions;
        } else {
          this.destinationSuggestions = suggestions;
        }

        this.loadingLocations = false;
      },
      error: () => {
        this.loadingLocations = false;
        this.locationError = 'Location list unavailable.';
        if (field === 'departure') {
          this.departureSuggestions = [];
        } else {
          this.destinationSuggestions = [];
        }
      },
    });
  }

  private scheduleDemandAlert(): void {
    if (this.demandTimeout) {
      clearTimeout(this.demandTimeout);
    }

    const departure = `${this.searchForm.value.departure || ''}`.trim();
    const destination = `${this.searchForm.value.destination || ''}`.trim();
    if (!departure || !destination) {
      this.demandAlert = undefined;
      this.demandAlertError = '';
      this.demandAlertLoading = false;
      return;
    }

    this.demandTimeout = setTimeout(() => {
      this.loadDemandAlert();
    }, 300);
  }

  private loadDemandAlert(): void {
    const departure = `${this.searchForm.value.departure || ''}`.trim();
    const destination = `${this.searchForm.value.destination || ''}`.trim();
    const dateFrom = `${this.searchForm.value.date || ''}`.trim() || undefined;
    const dateTo = `${this.searchForm.value.returnDate || ''}`.trim() || undefined;
    if (!departure || !destination) {
      return;
    }

    this.demandAlertLoading = true;
    this.demandAlertError = '';
    this.dataService.getDemandAlert(departure, destination, dateFrom, dateTo).subscribe({
      next: (alert) => {
        this.demandAlertLoading = false;
        this.demandAlert = alert;
      },
      error: () => {
        this.demandAlertLoading = false;
        this.demandAlert = undefined;
        this.demandAlertError = 'Demand alert unavailable.';
      },
    });
  }
>>>>>>> origin/feature/integrated-app-event
}
