import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { CarpoolingDataService } from '../services/carpooling-data.service';

interface LocationSuggestion {
  label: string;
  value: string;
  type: 'city';
}

@Component({
  selector: 'app-carpooling-home',
  templateUrl: './carpooling-home.component.html',
  styleUrls: ['./carpooling-home.component.css'],
})
export class CarpoolingHomeComponent {
  searchForm!: FormGroup;
  departureSuggestions: LocationSuggestion[] = [];
  destinationSuggestions: LocationSuggestion[] = [];
  showDepartureSuggestions = false;
  showDestinationSuggestions = false;
  loadingLocations = false;
  locationError = '';
  private searchTimeout: any = null;

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private dataService: CarpoolingDataService,
  ) {
    this.searchForm = this.fb.group({
      departure: [''],
      destination: [''],
      date: [''],
      returnDate: [''],
      seatsNeeded: [1, [Validators.required, Validators.min(1), Validators.max(8)]],
    });
  }

  get f() {
    return this.searchForm.controls;
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
        seatsNeeded: this.searchForm.value.seatsNeeded || 1,
      },
    });
  }

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
}
