import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { CarpoolingDataService, CountryCityData } from '../services/carpooling-data.service';

interface LocationSuggestion {
  label: string;
  value: string;
  type: 'city' | 'country';
}

@Component({
  selector: 'app-carpooling-home',
  templateUrl: './carpooling-home.component.html',
  styleUrls: ['./carpooling-home.component.css'],
})
export class CarpoolingHomeComponent implements OnInit {
  searchForm!: FormGroup;
  allLocations: LocationSuggestion[] = [];
  departureSuggestions: LocationSuggestion[] = [];
  destinationSuggestions: LocationSuggestion[] = [];
  showDepartureSuggestions = false;
  showDestinationSuggestions = false;
  loadingLocations = false;
  locationError = '';

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private dataService: CarpoolingDataService,
  ) {}

  ngOnInit(): void {
    this.searchForm = this.fb.group({
      departure: [''],
      destination: [''],
      date: ['', Validators.required],
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

  publishRide(): void {
    this.router.navigate(['/carpooling/publish']);
  }

  onLocationInput(field: 'departure' | 'destination'): void {
    this.ensureLocationsLoaded();
    const suggestions = this.getFilteredLocations(this.searchForm.value[field]);

    if (field === 'departure') {
      this.departureSuggestions = suggestions;
      this.showDepartureSuggestions = true;
      return;
    }

    this.destinationSuggestions = suggestions;
    this.showDestinationSuggestions = true;
  }

  openSuggestions(field: 'departure' | 'destination'): void {
    this.ensureLocationsLoaded();
    this.onLocationInput(field);
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

  private loadLocations(): void {
    this.loadingLocations = true;
    this.locationError = '';

    this.dataService.getCountriesAndCities().subscribe({
      next: (data: CountryCityData[]) => {
        this.allLocations = this.buildLocationList(data);
        this.loadingLocations = false;
      },
      error: () => {
        this.loadingLocations = false;
        this.locationError = 'Location list unavailable.';
      },
    });
  }

  private ensureLocationsLoaded(): void {
    if (this.loadingLocations || this.allLocations.length > 0) {
      return;
    }

    this.loadLocations();
  }

  private buildLocationList(data: CountryCityData[]): LocationSuggestion[] {
    const suggestions: LocationSuggestion[] = [];
    const labels = new Set<string>();

    data.forEach((item) => {
      const country = (item.country || '').trim();

      if (country && !labels.has(country.toLowerCase())) {
        suggestions.push({
          label: country,
          value: country,
          type: 'country',
        });
        labels.add(country.toLowerCase());
      }

      (item.cities || []).forEach((city) => {
        const cityName = (city || '').trim();
        const label = country ? `${cityName}, ${country}` : cityName;

        if (!cityName || labels.has(label.toLowerCase())) {
          return;
        }

        suggestions.push({
          label: label,
          value: cityName,
          type: 'city',
        });
        labels.add(label.toLowerCase());
      });
    });

    return suggestions;
  }

  private getFilteredLocations(value: string): LocationSuggestion[] {
    const searchValue = `${value || ''}`.trim().toLowerCase();

    if (!searchValue) {
      return this.allLocations.slice(0, 8);
    }

    return this.allLocations
      .filter((location) => location.label.toLowerCase().includes(searchValue))
      .slice(0, 8);
  }
}
